/*
 * This file is part of l2jserver <l2jserver.com>.
 *
 * l2jserver is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * l2jserver is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with l2jserver.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jserver.model.id.object;

import com.google.inject.Inject;
import com.l2jserver.model.dao.PetDAO;
import com.l2jserver.model.id.ObjectID;
import com.l2jserver.model.id.provider.IDProvider;
import com.l2jserver.model.world.Pet;

/**
 * An {@link ObjectID} instance representing an {@link Pet} object
 * <p>
 * Please, do not directly instantiate this class, use an {@link IDProvider}
 * instead.
 * 
 * @author <a href="http://www.rogiel.com">Rogiel</a>
 */
public final class PetID extends ActorID<Pet> {
	/**
	 * Data Access Object (DAO) for pets
	 */
	private final PetDAO petDao;

	/**
	 * @param id
	 *            the raw id
	 * @param petDao
	 *            the pet DAO
	 */
	@Inject
	protected PetID(int id, PetDAO petDao) {
		super(id);
		this.petDao = petDao;
	}

	@Override
	public Pet getObject() {
		return petDao.select(this);
	}
}