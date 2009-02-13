/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.river;

import org.jboss.marshalling.reflect.SerializableField;
import org.jboss.marshalling.reflect.SerializableClass;

/**
 *
 */
public final class SerializableClassDescriptor extends ClassDescriptor {
    private final SerializableClass serializableClass;
    private final ClassDescriptor superClassDescriptor;
    private final SerializableField[] fields;

    public SerializableClassDescriptor(final SerializableClass serializableClass, final ClassDescriptor superClassDescriptor, final SerializableField[] fields) throws ClassNotFoundException {
        super(serializableClass.getSubjectClass(), Protocol.ID_SERIALIZABLE_CLASS);
        this.serializableClass = serializableClass;
        this.superClassDescriptor = superClassDescriptor;
        this.fields = fields;
    }

    public ClassDescriptor getSuperClassDescriptor() {
        return superClassDescriptor;
    }

    public SerializableField[] getFields() {
        return fields;
    }

    public SerializableClass getSerializableClass() {
        return serializableClass;
    }
}