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

import net.oneandone.mork.classfile.attribute.Attribute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A class file. A class file defines a Java class.
 */

public class ClassDef extends Definition {
    public Set<Access> accessFlags;
    public ClassRef thisClass;
    public ClassRef superClass;
    public final List<ClassRef> interfaces;
    public final List<FieldDef> fields;
    public final List<MethodDef> methods;      // List of Method;
    public final List<Attribute> attributes;   // List of Attribute;

    /**
     * Create a new class file with the specified name and
     * super class name. Add interfaces, fields, methods and attributes
     * my modifying the respective public fields.
     */
    public ClassDef(ClassRef thisClass, ClassRef superClass) {
        this.accessFlags = new HashSet<Access>();
        accessFlags.add(Access.PUBLIC);
        accessFlags.add(Access.SUPER);
        this.thisClass = thisClass;
        this.superClass = superClass;
        this.interfaces = new ArrayList<ClassRef>();
        this.fields = new ArrayList<FieldDef>();
        this.methods = new ArrayList<MethodDef>();
        this.attributes = new ArrayList<Attribute>();
    }

    /**
     * Create a class file from the specified input stream.
     */
    public ClassDef(Input src) throws IOException {
        int i, max;

        interfaces = new ArrayList<ClassRef>();
        fields = new ArrayList<FieldDef>();
        methods = new ArrayList<MethodDef>();
        attributes = new ArrayList<Attribute>();

        accessFlags = Access.fromFlags(src.readU2(), true);
        thisClass = src.readClassRef();
        try {
            superClass = src.readClassRef();
        } catch (NullPointerException e) {
            if (!thisClass.isJavaLangObject()) {
                throw new RuntimeException(
                    "missing base class for " + thisClass);
            }
            superClass = null;
        }
        max = src.readU2();
        for (i = 0; i < max; i++) {
            interfaces.add(src.readClassRef());
        }
        max = src.readU2();
        for (i = 0; i < max; i++) {
            fields.add(new FieldDef(src));
        }
        max = src.readU2();
        for (i = 0; i < max; i++) {
            methods.add(new MethodDef(src));
        }
        max = src.readU2();
        for (i = 0; i < max; i++) {
            attributes.add(Attribute.create(src));
        }
    }

    public String getName() {
        return thisClass.name;
    }

    public MethodDef lookupMethod(MethodDef lm) {
        return lookupMethod(lm.accessFlags, lm.name, lm.argumentTypes, lm.returnType, lm.getExceptions());
    }

    public MethodDef lookupMethod(Set<Access> accessFlags, String name, ClassRef[] argumentTypes, ClassRef returnType,
            List<ClassRef> exceptions) {
        for (MethodDef m : methods) {
            if (m.accessFlags.equals(accessFlags) && m.name.equals(name)
                    && Arrays.equals(m.argumentTypes, argumentTypes)
                    && m.returnType.equals(returnType)
                    && m.getExceptions().equals(exceptions)) {
                return m;
            }
        }
        return null;
    }

    public MethodDef lookupMethod(String name, ClassRef ... argumentTypes) {
        for (MethodDef m : methods) {
            if (m.name.equals(name) && Arrays.equals(m.argumentTypes, argumentTypes)) {
                return m;
            }
        }
        return null;
    }

    public FieldDef lookupField(FieldDef field) {
        return lookupField(field.accessFlags, field.name, field.type);
    }

    public FieldDef lookupField(Set<Access> accessFlags, String name, ClassRef type) {
        for (FieldDef f : fields) {
            if (f.accessFlags.equals(accessFlags) && f.name.equals(name) && f.type.equals(type)) {
                return f;
            }
        }
        return null;
    }

    public FieldDef lookupField(String name) {
        for (FieldDef f : fields) {
            if (f.name.equals(name)) {
                return f;
            }
        }
        return null;
    }

    //--

    /**
     * Write this class file to the specified output stream.
     */
    public void write(Output dest) throws IOException {
        int i, max;

        dest.writeU2(Access.toFlags(accessFlags));
        dest.writeClassRef(thisClass);
        if (superClass != null) {
            dest.writeClassRef(superClass);
        } else {
            dest.writeU2(0);
        }

        max = interfaces.size();
        dest.writeU2(max);
        for (i = 0; i < max; i++) {
            dest.writeClassRef(interfaces.get(i));
        }
        max = fields.size();
        dest.writeU2(max);
        for (i = 0; i < max; i++) {
            fields.get(i).write(dest);
        }
        max = methods.size();
        dest.writeU2(max);
        for (i = 0; i < max; i++) {
            methods.get(i).write(dest);
        }
        max = attributes.size();
        dest.writeU2(max);
        for (i = 0; i < max; i++) {
            attributes.get(i).write(dest);
        }
    }

    //--

    public ClassRef reference() {
        return thisClass;
    }

    public FieldDef addField(Set<Access> access, ClassRef type, String name) {
        FieldDef f;

        f = new FieldDef(access, name, type);
        fields.add(f);
        return f;
    }

    public MethodDef addMethod(Set<Access> accessFlags, ClassRef result, String name, ClassRef[] args, Code code) {
        MethodDef m;

        m = new MethodDef();
        m.name = name;
        m.accessFlags = accessFlags;
        m.attributes.add(code);
        m.argumentTypes = args;
        m.returnType = result;
        methods.add(m);
        return m;
    }

    public MethodDef addConstructor(Set<Access> accessFlags, ClassRef[] args, Code code) {
        MethodDef m;

        m = new MethodDef();
        m.name = MethodRef.INIT;
        m.accessFlags = accessFlags;
        m.attributes.add(code);
        m.argumentTypes = args;
        m.returnType = ClassRef.VOID;
        methods.add(m);
        return m;
    }

    //--

    @Override
    public String toString() {
        StringBuilder result;
        int i, max;

        result = new StringBuilder();
        result.append("thisClass: " + thisClass + "\n");
        result.append("superClass: " + superClass + "\n");
        max = interfaces.size();
        for (i = 0; i < max; i++) {
            result.append("implements: " + interfaces.get(i) + "\n");
        }
        max = fields.size();
        for (i = 0; i < max; i++) {
            result.append("field: " + fields.get(i) + "\n");
        }
        max = methods.size();
        for (i = 0; i < max; i++) {
            result.append("method: " + methods.get(i) + "\n");
        }
        return result.toString();
    }

    public String toSignatureString() {
        StringBuilder result;
        int i, max;

        result = new StringBuilder();
        result.append(Access.toPrefix(accessFlags));
        result.append(getName());
        max = interfaces.size();
        if (!superClass.isJavaLangObject()) {
            result.append(" extends ");
            result.append(superClass.toString());
        }
        if (max > 0) {
            result.append(" implements");
            for (i = 0; i < max; i++) {
                result.append(' ');
                result.append(interfaces.get(i));
            }
        }
        return result.toString();
    }
}
