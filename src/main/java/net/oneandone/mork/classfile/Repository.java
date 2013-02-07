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
package net.oneandone.mork.classfile;

import net.oneandone.sushi.fs.Node;
import net.oneandone.sushi.fs.file.FileNode;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/** A set of class definitions */
public class Repository {
    private final Repository parent;
    private final Map<String, ClassDef> defs;
    private final List<Node> lazy;

    public Repository() {
        this(null);
    }
    public Repository(Repository parent) {
        this.parent = parent;
        this.defs = new HashMap<String, ClassDef>();
        this.lazy = new ArrayList<Node>();
    }

    private Node getDir(Node file) throws IOException {
        return file.isFile() && (file instanceof FileNode) ? ((FileNode) file).openZip() : file;
    }

    public void addAll(Node file) throws IOException {
        Node dir;

        file.checkExists();
        dir = getDir(file);
        for (Node node : dir.find("**/*.class")) {
            add(Input.load(node));
        }
    }

    public void addAllLazy(Node node) throws IOException {
        lazy.add(getDir(node));
    }

    public void add(ClassDef load) {
        defs.put(load.getName(), load);
    }

    public ClassDef lookup(String name) throws IOException {
        ClassDef result;
        Node file;
        String path;
        ClassDef def;

        def = defs.get(name);
        if (def != null) {
            return def;
        }
        if (parent != null) {
            result = parent.lookup(name);
            if (result != null) {
                return result;
            }
        }
        path = ClassRef.classToResName(name);
        for (Node dir : lazy) {
            file = dir.join(path);
            if (file.isFile()) {
                def = Input.load(file);
                add(def);
                return def;
            }
        }
        return null;
    }

    public ClassDef lookup(ClassDef c) throws IOException {
        ClassDef def;

        def = lookup(c.getName());
        if (def != null && def.accessFlags.equals(c.accessFlags) && def.superClass.equals(c.superClass)
                && def.interfaces.equals(c.interfaces)) {
            return def;
        } else {
            return null;
        }
    }

    public void dump(PrintWriter dest) {
        for (ClassDef def : defs.values()) {
            dest.println(def.toString());
        }
    }

    public void diff(Repository rightSet, PrintWriter info) throws IOException {
        ClassDef tmp;

        for (ClassDef left : defs.values()) {
            if (rightSet.lookup(left) == null) {
                info.println("- " + left.toSignatureString());
            }
        }
        for (ClassDef right : rightSet.defs.values()) {
            if (this.lookup(right) == null) {
                info.println("+ " + right.toSignatureString());
            }
        }
        for (ClassDef left : defs.values()) {
            tmp = rightSet.lookup(left);
            if (tmp != null) {
                diffBody(left, tmp, info);
            }
        }
    }

    public void defines(List<Reference> pblic, List<Reference> prvate) {
        ClassRef owner;

        for (ClassDef def : defs.values()) {
            owner = def.reference();
            for (MethodDef m : def.methods) {
                (m.accessFlags.contains(Access.PUBLIC) ? pblic : prvate).
                    add(m.reference(owner, def.accessFlags.contains(Access.INTERFACE)));
            }
            for (FieldDef f : def.fields) {
                (f.accessFlags.contains(Access.PUBLIC) ? pblic : prvate).add(f.reference(owner));
            }
        }
    }

    public static void diffBody(ClassDef left, ClassDef right, PrintWriter info) {
        List<FieldDef> removedFields;
        List<FieldDef> addedFields;
        List<MethodDef> removedMethods;
        List<MethodDef> addedMethods;

        removedFields = new ArrayList<>();
        addedFields = new ArrayList<>();
        for (FieldDef lf : left.fields) {
            if (right.lookupField(lf) == null) {
                removedFields.add(lf);
            }
        }
        for (FieldDef rf : right.fields) {
            if (left.lookupField(rf) == null) {
                addedFields.add(rf);
            }
        }

        removedMethods = new ArrayList<MethodDef>();
        addedMethods = new ArrayList<MethodDef>();
        for (MethodDef lm : left.methods) {
            if (right.lookupMethod(lm) == null) {
                removedMethods.add(lm);
            }
        }
        for (MethodDef rm : right.methods) {
            if (left.lookupMethod(rm) == null) {
                addedMethods.add(rm);
            }
        }

        if (removedFields.size() > 0 || addedFields.size() > 0
                || removedMethods.size() > 0 || addedMethods.size() > 0) {
            info.println("* " + left.toSignatureString());
            for (FieldDef f : removedFields) {
                info.println("  - " + f.toString() + ";");
            }
            for (FieldDef f : addedFields) {
                info.println("  + " + f.toString() + ";");
            }
            for (MethodDef m : removedMethods) {
                info.println("  - " + m.toSignatureString());
            }
            for (MethodDef m : addedMethods) {
                info.println("  + " + m.toSignatureString());
            }
        }
    }

    public void ref(List<Usage> result) {
        Code code;
        java.util.Set<Reference> refs;
        ClassRef owner;
        MethodRef from;

        for (ClassDef def : defs.values()) {
            owner = def.reference();
            for (MethodDef m : def.methods) {
                code = m.getCode();
                if (code != null) {
                    from = m.reference(owner, def.accessFlags.contains(Access.INTERFACE));
                    refs = new HashSet<Reference>();
                    code.references(refs);
                    for (Reference ref : refs) {
                        result.add(new Usage(from, ref));
                    }
                }
            }
        }
    }
}
