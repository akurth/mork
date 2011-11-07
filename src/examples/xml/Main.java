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

package xml;

import net.sf.beezle.mork.mapping.Mapper;

/**
 * This is a kind of XML scanner. It does not read external entities; in fact, entities are
 * not processed at all ...
 */

public class Main {
    public static void main(String[] args) {
        int i;
        int ok;
        long tmp;

        if (args.length == 0) {
            System.out.println("XML Parser");
            System.out.println("usage: xml.Main <filename>+");
        } else {
            load();
            tmp = System.currentTimeMillis();
            ok = 0;
            for (i = 0; i < args.length; i++) {
                if (parse(args[i])) {
                    ok++;
                }
            }
            System.out.println(ok + "/" + args.length + " parsed successfully.");
            System.out.println((System.currentTimeMillis() - tmp) + " ms");
        }
    }

    private static Mapper mapper;

    private static void load() {
        long tmp;

        System.out.print("loading mapper ... ");
        tmp = System.currentTimeMillis();
        mapper = new Mapper("xml.Mapper");
        System.out.println("done (" + (System.currentTimeMillis() - tmp) + " ms)");
        if ("true".equals(System.getProperty("mork.verbose"))) {
            mapper.setLogging(System.out, null);
        }
    }

    private static boolean parse(String name) {
        return mapper.run(name) != null;
    }
}
