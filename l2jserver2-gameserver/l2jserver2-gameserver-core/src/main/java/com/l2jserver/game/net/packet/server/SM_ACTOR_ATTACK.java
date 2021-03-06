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
package com.l2jserver.game.net.packet.server;

import java.util.Collections;
import java.util.List;

import org.jboss.netty.buffer.ChannelBuffer;

import com.l2jserver.model.server.AttackHit;
import com.l2jserver.model.world.Actor;
import com.l2jserver.service.network.model.Lineage2Client;
import com.l2jserver.service.network.model.packet.AbstractServerPacket;
import com.l2jserver.util.factory.CollectionFactory;

/**
 * This packet informs the client of an attack issued
 * 
 * @author <a href="http://www.rogiel.com">Rogiel</a>
 * @see AttackHit
 */
public class SM_ACTOR_ATTACK extends AbstractServerPacket {
	/**
	 * The packet OPCODE
	 */
	public static final int OPCODE = 0x05;

	/**
	 * The attacker actor
	 */
	private final Actor attacker;
	/**
	 * List of hits to be sent
	 */
	private final List<AttackHit> hits = CollectionFactory.newList();

	/**
	 * @param attacker
	 *            the attacked
	 * @param hits
	 *            the hits
	 */
	public SM_ACTOR_ATTACK(Actor attacker, AttackHit... hits) {
		super(OPCODE);
		this.attacker = attacker;
		Collections.addAll(this.hits, hits);
	}

	/**
	 * @param hits
	 *            the hits
	 */
	public SM_ACTOR_ATTACK(AttackHit... hits) {
		this(hits[0].getAttacker(), hits);
	}

	@Override
	public void write(Lineage2Client conn, ChannelBuffer buffer) {
		buffer.writeInt(attacker.getID().getID());

		final AttackHit first = hits.get(0);
		buffer.writeInt(first.getTarget().getID().getID());
		buffer.writeInt((int) first.getDamage());
		buffer.writeByte(first.getFlagsByte());

		buffer.writeInt(attacker.getPoint().getX());
		buffer.writeInt(attacker.getPoint().getY());
		buffer.writeInt(attacker.getPoint().getZ());

		buffer.writeShort(hits.size() - 1);
		if (hits.size() > 1) {
			boolean skipFirst = false;
			for (final AttackHit hit : hits) {
				if (!skipFirst) {
					skipFirst = true;
					continue;
				}
				buffer.writeInt(hit.getTarget().getID().getID());
				buffer.writeInt((int) hit.getDamage());
				buffer.writeByte(hit.getFlagsByte());
			}
		}

		buffer.writeInt(first.getTarget().getPoint().getX());
		buffer.writeInt(first.getTarget().getPoint().getY());
		buffer.writeInt(first.getTarget().getPoint().getZ());
	}

	/**
	 * Adds a new hit
	 * 
	 * @param hit
	 *            the hit
	 * @return this instance
	 */
	public SM_ACTOR_ATTACK add(AttackHit hit) {
		hits.add(hit);
		return this;
	}
}
