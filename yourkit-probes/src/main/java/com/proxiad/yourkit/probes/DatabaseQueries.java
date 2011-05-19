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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.yourkit.probes.MethodPattern;
import com.yourkit.probes.OnEnterResult;
import com.yourkit.probes.Param;
import com.yourkit.probes.ReturnValue;
import com.yourkit.probes.StringColumn;
import com.yourkit.probes.Table;
import com.yourkit.probes.This;

/**
 * This probe handles SQL queries. It differs from Yourkit's built'in Databases
 * probe in that it logs all SQL queries flat, which eases viewing and exporting
 * to CSV when all you want is to visualize queries and you don't care about
 * their relationship to connections and statements.
 * 
 * @author <a href="mailto:c.vidal@proxiad.com">Cédric Vidal, ProxiAD</a>
 * 
 */
public final class DatabaseQueries {

	private static final ConcurrentMap<Object, String> ourStatement2Query = new ConcurrentHashMap<Object, String>();

	private static final class QueryTable extends Table {
		private final StringColumn mySQL = new StringColumn("SQL");

		public QueryTable() {
			super("Queries", Table.MASK_FOR_SINGLE_METHOD_CALL_LASTING_EVENTS);
		}
	}

	private static final QueryTable T_QUERY = new QueryTable();

	private static final class PreparedStatementQueryTable extends Table {
		private final StringColumn mySQL = new StringColumn("SQL");

		public PreparedStatementQueryTable() {
			super("Prepared Statement Queries",
					Table.MASK_FOR_SINGLE_METHOD_CALL_LASTING_EVENTS);
		}
	}

	private static final PreparedStatementQueryTable T_PREPARED_STATEMENT_QUERY = new PreparedStatementQueryTable();

	@MethodPattern({ "*:execute(String, int)", "*:execute(String)",
			"*:execute(String, int[])", "*:execute(String, String[])",
			"*:executeQuery(String)", "*:executeUpdate(String, int[])",
			"*:executeUpdate(String)", "*:executeUpdate(String, int)",
			"*:executeUpdate(String, String[])" })
	public static final class Statement_execute_Probe {
		public static int onEnter(final @This Statement statement,
				final @Param(1) String query) {
			if (statement instanceof PreparedStatement) {
				return -1;
			}

			final int rowIndex = T_QUERY.createRow();
			T_QUERY.mySQL.setValue(rowIndex, query == null ? "<unknown>"
					: query);
			return rowIndex;
		}

		public static void onReturn(final @OnEnterResult int rowIndex) {
			if (rowIndex != -1) {
				T_QUERY.closeRow(rowIndex);
			}
		}
	}

	@MethodPattern({ "*:execute()", "*:executeQuery()", "*:executeUpdate()" })
	public static final class PreparedStatement_execute_Probe {
		public static int onEnter(final @This PreparedStatement statement) {
			final String query = ourStatement2Query.get(statement);
			final int rowIndex = T_PREPARED_STATEMENT_QUERY.createRow();
			T_PREPARED_STATEMENT_QUERY.mySQL.setValue(rowIndex,
					query == null ? "<unknown>" : query);
			return rowIndex;
		}

		public static void onReturn(final @OnEnterResult int rowIndex) {
			if (rowIndex != -1) {
				T_PREPARED_STATEMENT_QUERY.closeRow(rowIndex);
			}
		}
	}

	@MethodPattern({ "*:prepareCall(String, int, int, int)",
			"*:prepareCall(String, int, int)", "*:prepareCall(String)",
			"*:prepareStatement(String, int)",
			"*:prepareStatement(String, String[])",
			"*:prepareStatement(String, int, int, int)",
			"*:prepareStatement(String)",
			"*:prepareStatement(String, int, int)",
			"*:prepareStatement(String, int[])" })
	public static final class Connection_prepareStatement_Probe {
		public static void onReturn(final @This Connection connection,
				final @Param(1) String query,
				final @ReturnValue PreparedStatement preparedStatement) {
			if (preparedStatement == null) {
				return;
			}

			ourStatement2Query.putIfAbsent(preparedStatement, query);
		}
	}

}
