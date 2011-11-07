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

package config;

import net.sf.beezle.mork.mapping.Mapper;
import net.sf.beezle.mork.misc.GenericException;
import java.util.Properties;

/**
 * Pathologic examples for an application with a configuration file.
 * A configuration is simply list of key-value pairs; it's mapped into
 * a Java Properties object.
 *
 * Note: If a configuration file is not overly complex, I prefer using XML syntax
 * because it's easy to learn for the user of the application. In contrast, configuration
 * files with a special purpose syntax (defined by a grammar syntax) are harder to learn
 * - but more powerful because you can define things like arithmetic expressions.
 */

public class Main {
    /**
     * Reads the file(s) specified on the command line and prints it to the console.
     */
    public static void main(String[] args) {
        Mapper mapper;
        Object[] result;
        Properties props;
        int i;

        if (args.length == 0) {
            System.out.println("usage: config.Main <filename>");
            return;
        }

        mapper = new Mapper("config.Mapper");
        for (i = 0; i < args.length; i++) {
            System.out.println(args[i] + ":");
            result = mapper.run(args[i]);
            if (result == null) {
                System.err.println("error(s) - aborted.");
                return;
            }
            props = (Properties) result[0];
            System.out.println(props.toString());
        }
    }

    public static Properties config(String[] keys, Object[] values) {
        int i;
        Properties props;

        if (keys.length != values.length) {
            throw new IllegalArgumentException();
        }
        props = new Properties();
        for (i = 0; i < keys.length; i++) {
            props.put(keys[i], values[i]);
        }
        return props;
    }

    // I need this because PCTEXT is optional -- and null means the empty string
    public static String str(String str) {
        if (str != null) {
            return str;
        } else {
            return "";
        }
    }

    public static int number(String radixStr, String str) throws GenericException {
        int radix;

        if (str == null) {
            str = "";
        }
        try {
            radix = Integer.parseInt(radixStr);
        } catch (NumberFormatException e) {
            throw new GenericException("invalid radix: " + radixStr);
        }
        try {
            return Integer.parseInt(str, radix);
        } catch (NumberFormatException e) {
            throw new GenericException("invalid number (for radix " + radix + "): " + str);
        }
    }
}
