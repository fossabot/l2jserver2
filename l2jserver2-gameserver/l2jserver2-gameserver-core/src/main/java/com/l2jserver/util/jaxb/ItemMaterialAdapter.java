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
package com.l2jserver.util.jaxb;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.l2jserver.model.template.item.ItemMaterial;

/**
 * This class is an JAXB Adapter for {@link ItemMaterial}
 * 
 * @author <a href="http://www.rogiel.com">Rogiel</a>
 */
public class ItemMaterialAdapter extends XmlAdapter<String, ItemMaterial> {
	@Override
	public ItemMaterial unmarshal(String v) throws Exception {
		return ItemMaterial.valueOf(v);
	}

	@Override
	public String marshal(ItemMaterial v) throws Exception {
		if (v == null)
			return null;
		return v.name();
	}
}
