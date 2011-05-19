/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.proxiad.yourkit.probes;

import java.net.URL;

import com.yourkit.probes.MethodPattern;
import com.yourkit.probes.OnEnterResult;
import com.yourkit.probes.Param;
import com.yourkit.probes.StringColumn;
import com.yourkit.probes.Table;

/**
 * This Yourkit probe handles Apache SOAP RPC calls.
 * 
 * @author <a href="mailto:c.vidal@proxiad.com">Cédric Vidal, ProxiAD</a>
 * 
 */
public final class ApacheSoapRpc {

	private static final class ApacheSoapRpcCallTable extends Table {
		private final StringColumn myURL = new StringColumn("URL");
		private final StringColumn myMessage = new StringColumn("Message");

		public ApacheSoapRpcCallTable() {
			super("RPC/Apache SOAP RPC",
					Table.MASK_FOR_SINGLE_METHOD_CALL_LASTING_EVENTS);
		}
	}

	private static final ApacheSoapRpcCallTable T_REQUEST = new ApacheSoapRpcCallTable();

	@MethodPattern("*:invoke(java.net.URL, java.lang.String)")
	public static final class Call_invoke_Probe {
		public static int onEnter(final @Param(1) URL url,
				final @Param(2) String message) {

			final int rowIndex = T_REQUEST.createRow();
			T_REQUEST.myURL.setValue(rowIndex, url.toString());
			T_REQUEST.myMessage.setValue(rowIndex, message);
			return rowIndex;
		}

		public static void onReturn(final @OnEnterResult int rowIndex) {
			if (rowIndex == -1) {
				return;
			}

			T_REQUEST.closeRow(rowIndex);
		}
	}

}
