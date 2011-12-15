/*
 * This file is part of l2jserver2 <l2jserver2.com>.
 *
 * l2jserver2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * l2jserver2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with l2jserver2.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jserver.service.game.item;

import java.util.Arrays;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.l2jserver.game.net.packet.client.CM_CHAR_ACTION.CharacterAction;
import com.l2jserver.model.Model.ObjectDesire;
import com.l2jserver.model.dao.ItemDAO;
import com.l2jserver.model.id.object.ItemID;
import com.l2jserver.model.id.object.provider.ItemIDProvider;
import com.l2jserver.model.id.template.ItemTemplateID;
import com.l2jserver.model.world.Actor;
import com.l2jserver.model.world.Item;
import com.l2jserver.model.world.L2Character;
import com.l2jserver.model.world.character.CharacterInventory.ItemLocation;
import com.l2jserver.model.world.item.ItemDropEvent;
import com.l2jserver.model.world.item.ItemPickUpEvent;
import com.l2jserver.service.AbstractService;
import com.l2jserver.service.AbstractService.Depends;
import com.l2jserver.service.ServiceStartException;
import com.l2jserver.service.ServiceStopException;
import com.l2jserver.service.database.DatabaseService;
import com.l2jserver.service.game.spawn.AlreadySpawnedServiceException;
import com.l2jserver.service.game.spawn.NotSpawnedServiceException;
import com.l2jserver.service.game.spawn.SpawnPointNotFoundServiceException;
import com.l2jserver.service.game.spawn.SpawnService;
import com.l2jserver.service.game.world.WorldService;
import com.l2jserver.service.game.world.event.WorldEventDispatcher;
import com.l2jserver.util.geometry.Point3D;

/**
 * @author <a href="http://www.rogiel.com">Rogiel</a>
 */
@Depends({ SpawnService.class, DatabaseService.class })
public class ItemServiceImpl extends AbstractService implements ItemService {
	/**
	 * The item DAO
	 */
	private final ItemDAO itemDao;
	/**
	 * The Spawn Service
	 */
	private final SpawnService spawnService;
	/**
	 * The {@link WorldService} event dispatcher
	 */
	private final WorldEventDispatcher eventDispatcher;
	/**
	 * The {@link ItemID} provider
	 */
	private final ItemIDProvider itemIdProvider;

	/**
	 * All items on the ground persisted to the database
	 */
	private List<Item> items;

	/**
	 * @param itemDao
	 *            the item DAO
	 * @param spawnService
	 *            the spawn service
	 * @param eventDispatcher
	 *            the world service event dispatcher
	 * @param itemIdProvider
	 *            the {@link ItemID} provider
	 */
	@Inject
	private ItemServiceImpl(ItemDAO itemDao, SpawnService spawnService,
			WorldEventDispatcher eventDispatcher, ItemIDProvider itemIdProvider) {
		this.itemDao = itemDao;
		this.spawnService = spawnService;
		this.eventDispatcher = eventDispatcher;
		this.itemIdProvider = itemIdProvider;
	}

	@Override
	protected void doStart() throws ServiceStartException {
		items = itemDao.loadDroppedItems();
		try {
			for (final Item item : items) {
				spawnService.spawn(item, null);
			}
		} catch (SpawnPointNotFoundServiceException e) {
			throw new ServiceStartException(e);
		} catch (AlreadySpawnedServiceException e) {
			throw new ServiceStartException(e);
		}
	}

	@Override
	public Item action(Item item, L2Character character, CharacterAction action)
			throws ItemNotOnGroundServiceException, NotSpawnedServiceException {
		switch (action) {
		case CLICK:
			return pickUp(item, character);
		case RIGHT_CLICK:
			return item;
		}
		return item;
	}

	@Override
	public Item split(Item item, long count)
			throws NotEnoughItemsServiceException {
		if (item.getCount() < count)
			throw new NotEnoughItemsServiceException();
		if (item.getCount() == count)
			return item;

		final Item splitItem = item.getTemplate().create();
		splitItem.setID(itemIdProvider.createID());
		splitItem.setCount(count);
		item.setCount(item.getCount() - count);

		splitItem.setObjectDesire(ObjectDesire.INSERT);

		return splitItem;
	}

	@Override
	public Item stack(Item... items) throws NonStackableItemsServiceException {
		Preconditions.checkState(items.length >= 2,
				"items length must be 2 or greater");
		// TODO implement real item stacking

		final ItemTemplateID templateID = items[0].getTemplateID();
		for (final Item item : items) {
			if (!item.getTemplateID().equals(templateID))
				throw new NonStackableItemsServiceException();
		}

		final Item item = items[0];
		for (int i = 1; i < items.length; i++) {
			item.setCount(item.getCount() + items[i].getCount());
		}

		return item;
	}

	@Override
	public Item pickUp(Item item, L2Character character)
			throws ItemNotOnGroundServiceException, NotSpawnedServiceException {
		synchronized (item) {
			if (item.getLocation() != ItemLocation.GROUND)
				throw new ItemNotOnGroundServiceException();

			final Item originalItem = item;

			item.setLocation(ItemLocation.INVENTORY);
			item.setPaperdoll(null);
			item.setOwnerID(character.getID());
			final Item[] items = character.getInventory().getItems(
					item.getTemplateID());
			if (items.length != 0) {
				Item[] stackItems = Arrays.copyOf(items, items.length + 1);
				stackItems[items.length] = item;
				try {
					item = stack(stackItems);
					Item[] removedItems = character.getInventory().remove(
							stackItems);
					for (final Item removeItem : removedItems) {
						if (!removeItem.equals(item)) {
							itemDao.delete(removeItem);
							itemIdProvider.destroy(removeItem.getID());
						}
					}
					character.getInventory().add(item);
				} catch (NonStackableItemsServiceException e) {
					character.getInventory().add(item);
				}
			} else {
				character.getInventory().add(item);
			}

			character.getInventory().add(item);
			this.items.remove(item);

			itemDao.save(item);
			spawnService.unspawn(originalItem);
			eventDispatcher.dispatch(new ItemPickUpEvent(character,
					originalItem, item));

			return item;
		}
	}

	@Override
	public Item drop(Item item, long count, Point3D point, Actor actor)
			throws SpawnPointNotFoundServiceException,
			ItemAlreadyOnGroundServiceException,
			AlreadySpawnedServiceException, NotEnoughItemsServiceException {
		synchronized (item) {
			if (item.getLocation() == ItemLocation.GROUND)
				throw new AlreadySpawnedServiceException();

			final Item sourceItem = item;
			item = split(sourceItem, count);

			item.setLocation(ItemLocation.GROUND);
			item.setPaperdoll(null);

			spawnService.spawn(item, point);
			eventDispatcher.dispatch(new ItemDropEvent(actor, item));

			if (actor instanceof L2Character) {
				if (sourceItem.equals(item)) {
					((L2Character) actor).getInventory().remove(item);
				}
			}

			itemDao.save(item);
			if (!item.equals(sourceItem)) {
				itemDao.save(sourceItem);
			}
			items.add(item);

			return item;
		}
	}

	@Override
	public void drop(Item item, Point3D point, Actor actor)
			throws SpawnPointNotFoundServiceException,
			ItemAlreadyOnGroundServiceException,
			AlreadySpawnedServiceException, NotEnoughItemsServiceException {
		drop(item, item.getCount(), point, actor);
	}

	@Override
	protected void doStop() throws ServiceStopException {
		try {
			for (final Item item : items) {
				spawnService.unspawn(item);
			}
		} catch (NotSpawnedServiceException e) {
			throw new ServiceStopException("Item is not spawned anymore", e);
		}
	}
}