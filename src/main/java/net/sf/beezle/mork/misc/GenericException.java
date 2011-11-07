/*
 * Copyright 1&1 Internet AG, http://www.1and1.org
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.sf.beezle.mork.misc;

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
