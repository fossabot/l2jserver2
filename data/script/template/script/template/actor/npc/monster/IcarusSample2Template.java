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
package script.template.actor.npc.monster;

import com.google.inject.Inject;
import com.l2jserver.model.id.template.provider.ItemTemplateIDProvider;
import com.l2jserver.model.id.template.provider.NPCTemplateIDProvider;
import com.l2jserver.model.template.npc.MonsterNPCTemplate;
import com.l2jserver.model.world.Actor.ActorSex;

/**
 * @author <a href="http://www.rogiel.com">Rogiel</a>
 */
public class IcarusSample2Template extends MonsterNPCTemplate {
	public static final int ID = 25116;

	@Inject
	protected IcarusSample2Template(NPCTemplateIDProvider provider, ItemTemplateIDProvider itemProvider) {
		super(provider.createID(ID));
		this.name = "Icarus Sample 2";
		this.serverSideName = false;
		this.title = "Raid Fighter";
		this.serverSideTitle = false;
		this.collisionRadius = 11.00;
		this.collisionHeight = 27.00;
		this.level = 39;
		this.sex = ActorSex.MALE;
		this.attackRange = 1100;
		this.maxHP = 12518.769482355200000;
		this.maxMP = 463.320000000000000;
		this.hpRegeneration = 43.476781636636500;
		this.mpRegeneration = 1.800000000000000;
		this.experience = 0;
		this.sp = 0;
		this.aggressive = false;
		this.rightHand = itemProvider.createID(273);
		this.leftHand = null;
		this.enchantLevel = 0;
		this.targetable = true;
		this.showName = true;
		this.dropHerbGroup = 0;
		this.baseAttributes = true;
		
		attributes.intelligence = 21;
		attributes.strength = 40;
		attributes.concentration = 43;
		attributes.mentality = 20;
		attributes.dexterity = 30;
		attributes.witness = 20;
		attributes.physicalAttack = 822.67619;
		attributes.magicalAttack = 239.88040;
		attributes.physicalDefense = 228.70128;
		attributes.magicalDefense = 223.14000;
		attributes.attackSpeed = 253;
		attributes.castSpeed = 333;
		attributes.criticalChance = 8;
		attributes.walkSpeed = 40.00000;
		attributes.runSpeed = 170.00000;
	}
}