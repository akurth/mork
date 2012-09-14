/**
 * Copyright 1&1 Internet AG, https://github.com/1and1/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.oneandone.mork.misc;

/**
 * Exception with a type. Using this class avoids defining may exceptions
 * classes to distinguish different types of exceptions.
 */

public class GenericException extends Exception {
    public final String id;      // != null, used to identify the exception
    public final String details; // != null
    public final Throwable base; // nullable

    public GenericException(String id) {
        this(id, "", null);
    }

    public GenericException(String id, String details) {
        this(id, details, null);
    }

    public GenericException(String id, Throwable base) {
        this(id, "", base);
    }

    public GenericException(String id, String details, Throwable base) {
        // adding details to the message improves the error message
        // you get by just printing the exception
        super(str(id, details));

        this.id = id;
        this.details = details;
        this.base = base;
    }

    private static String str(String msg, String details) {
        if (details.length() != 0) {
            return msg + ":" + details;
        } else {
            return msg;
        }
    }

    @Override
    public String toString() {
        return getMessage();
    }
}
