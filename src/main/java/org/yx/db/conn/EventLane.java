/**
 * Copyright (C) 2016 - 2017 youtongluan.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.yx.db.conn;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.yx.db.event.DBEvent;
import org.yx.db.event.DBEventPublisher;
import org.yx.db.event.ModifyEvent;

public class EventLane {
	private static ThreadLocal<Map<Connection, EventLane>> POOL = new ThreadLocal<Map<Connection, EventLane>>() {
		@Override
		protected Map<Connection, EventLane> initialValue() {
			return new HashMap<Connection, EventLane>();
		}

	};
	private List<DBEvent> events = new ArrayList<DBEvent>();

	public EventLane() {
	}

	private static EventLane pool(Connection conn) {
		return POOL.get().get(conn);
	}

	public static void pubuish(Connection conn, DBEvent event) {
		if (event == null) {
			return;
		}
		EventLane pool = pool(conn);
		if (pool == null) {
			pool = new EventLane();
			POOL.get().put(conn, pool);
		}
		if (ModifyEvent.class.isInstance(event)) {
			pool.events.add(event);
		} else {
			DBEventPublisher.publish(event);
		}
	}

	static void realPubuish(Connection conn) {
		EventLane pool = pool(conn);
		if (pool == null) {
			return;
		}
		for (DBEvent event : pool.events) {
			DBEventPublisher.publish(event);
		}
	}

	public static void remove(Connection conn) {
		POOL.get().remove(conn);
	}

	public static void removeALL() {
		POOL.remove();
	}
}
