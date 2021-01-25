/*--
 * #%L
 * Cognifide Actions
 * %%
 * Copyright (C) 2015 Wunderman Thompson Technology
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package com.cognifide.actions.core.serializer;

import java.util.Calendar;

public enum ValueMapType {
	STRING(String.class) {
		@Override
		protected String doSerialize(Object o) {
			return (String) o;
		}

		@Override
		public Object deserialize(String string) {
			return string;
		}
	},
	INTEGER(Integer.class) {
		@Override
		protected String doSerialize(Object o) {
			return Integer.toString((Integer) o);
		}

		@Override
		public Object deserialize(String string) {
			return Integer.parseInt(string);
		}
	},
	LONG(Long.class) {
		@Override
		protected String doSerialize(Object o) {
			return Long.toString((Long) o);
		}

		@Override
		public Object deserialize(String string) {
			return Long.parseLong(string);
		}
	},
	DOUBLE(Double.class) {
		@Override
		protected String doSerialize(Object o) {
			return Double.toString((Double) o);
		}

		@Override
		public Object deserialize(String string) {
			return Double.parseDouble(string);
		}
	},
	BOOLEAN(Boolean.class) {
		@Override
		protected String doSerialize(Object o) {
			return Boolean.toString((Boolean) o);
		}

		@Override
		public Object deserialize(String string) {
			return Boolean.parseBoolean(string);
		}
	},
	FLOAT(Float.class) {
		@Override
		protected String doSerialize(Object o) {
			return Float.toString((float) o);
		}

		@Override
		public Object deserialize(String string) {
			return Float.parseFloat(string);
		}
	},
	BYTE(Byte.class) {
		@Override
		protected String doSerialize(Object o) {
			return Byte.toString((byte) o);
		}

		@Override
		public Object deserialize(String string) {
			return Byte.parseByte(string);
		}
	},
	CHARACTER(Character.class) {
		@Override
		protected String doSerialize(Object o) {
			return Character.toString((char) o);
		}

		@Override
		public Object deserialize(String string) {
			return (char) Integer.parseInt(string);
		}
	},
	CALENDAR(Calendar.class) {
		@Override
		protected String doSerialize(Object o) {
			final Calendar calendar = (Calendar) o;
			return Long.toString(calendar.getTimeInMillis());
		}

		@Override
		public Object deserialize(String string) {
			final long millis = Long.parseLong(string);
			final Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(millis);
			return cal;
		}
	};

	private final Class<?> type;

	private ValueMapType(Class<?> type) {
		this.type = type;
	}

	public Class<?> getType() {
		return type;
	}

	public boolean isSupported(Object o) {
		return o != null && type.isAssignableFrom(o.getClass());
	}

	public String serialize(Object o) {
		if (!isSupported(o)) {
			throw new IllegalArgumentException(o + " can't be serialized with " + this);
		}
		return doSerialize(o);
	}

	protected abstract String doSerialize(Object o);

	public abstract Object deserialize(String string);
}
