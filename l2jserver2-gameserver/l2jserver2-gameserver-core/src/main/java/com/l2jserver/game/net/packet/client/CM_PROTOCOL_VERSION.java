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
package com.l2jserver.game.net.packet.client;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.l2jserver.L2JConstant;
import com.l2jserver.game.net.packet.server.SM_KEY;
import com.l2jserver.service.network.keygen.BlowfishKeygenService;
import com.l2jserver.service.network.model.Lineage2Client;
import com.l2jserver.service.network.model.Lineage2CryptographyKey;
import com.l2jserver.service.network.model.ProtocolVersion;
import com.l2jserver.service.network.model.packet.AbstractClientPacket;

/**
 * In this packet the client is informing its protocol version. It is possible
 * to do an test and refuse invalid protocol versions. After this packet, the
 * messages received and sent are all encrypted, except for the encryptation key
 * which is sent here.
 * 
 * @author <a href="http://www.rogiel.com">Rogiel</a>
 */
public class CM_PROTOCOL_VERSION extends AbstractClientPacket {
	/**
	 * The packet OPCODE
	 */
	public static final int OPCODE = 0x00;

	/**
	 * The logger
	 */
	private final Logger log = LoggerFactory
			.getLogger(CM_PROTOCOL_VERSION.class);

	// services
	/**
	 * The {@link BlowfishKeygenService} implementation. Use to generate
	 * cryptography keys.
	 */
	private final BlowfishKeygenService keygen;

	// packet
	/**
	 * The client version of the protocol
	 */
	private ProtocolVersion version;

	/**
	 * @param keygen
	 *            the keygen service
	 */
	@Inject
	public CM_PROTOCOL_VERSION(BlowfishKeygenService keygen) {
		this.keygen = keygen;
	}

	@Override
	public void read(Lineage2Client conn, ChannelBuffer buffer) {
		this.version = ProtocolVersion.fromVersion((int) buffer
				.readShort());
	}

	@Override
	public void process(final Lineage2Client conn) {
		// generate a new key
		final Lineage2CryptographyKey inKey = new Lineage2CryptographyKey(
				keygen.generate(128));

		conn.enableDecrypter(inKey);
		final Lineage2CryptographyKey outKey = inKey.copy();
		log.debug("Decrypter has been enabled");

		log.debug("Client protocol version: {}", version);
		conn.setVersion(version);
		if (L2JConstant.SUPPORTED_PROTOCOL != version) {
			log.warn("Incorrect protocol version: {}. Only {} is supported.",
					version, L2JConstant.SUPPORTED_PROTOCOL);
			// notify wrong protocol and close connection
			conn.write(new SM_KEY(inKey, false)).addListener(
					new ChannelFutureListener() {
						@Override
						public void operationComplete(ChannelFuture future)
								throws Exception {
							// close connection
							conn.close();
						}
					});
			return;
		}
		// activate encrypter once packet has been sent.
		conn.write(new SM_KEY(inKey, true)).addListener(
				new ChannelFutureListener() {
					@Override
					public void operationComplete(ChannelFuture future)
							throws Exception {
						log.debug("Encrypter has been enabled");
						// enable encrypter
						conn.enableEncrypter(outKey);
					}
				});
	}
}
