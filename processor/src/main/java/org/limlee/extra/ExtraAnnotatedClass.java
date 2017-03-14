package org.limlee.extra;

import org.limlee.annotation.Extra;

import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

public class ExtraAnnotatedClass {
    VariableElement mVarElement;
    String mFiledName;
    TypeMirror mType;
    String mFileKeyName;
    boolean mIsPrivate;

    public ExtraAnnotatedClass(VariableElement element) {
        mVarElement = element;
        mFiledName = element.getSimpleName().toString();
        mType = element.asType();
        mIsPrivate = ProcessorHelper.isPrivateElement(element);
        Extra annination = mVarElement.getAnnotation(Extra.class);
        if (null != annination) {
            mFileKeyName = annination.value();
        }
        if (null == mFileKeyName
                || mFileKeyName.length() == 0) {
            mFileKeyName = getFiledName(mFiledName);
        }
    }

    public String getFiledName() {
        return getFiledName(mFiledName);
    }

    public static String getFiledName(String name) {
        if (name.matches("^m[A-Z]{1}")) {
            return name.substring(1, 2).toLowerCase();
        } else if (name.matches("^m[A-Z]{1}.*")) {
            return name.substring(1, 2).toLowerCase() + name.substring(2);
        }
        return name;
    }

    @Override
    public int hashCode() {
        return mFiledName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (null != obj
                && obj instanceof ExtraAnnotatedClass
                && ((ExtraAnnotatedClass) obj).mFiledName.equals(this.mFiledName)) {
            return true;
        }
        return false;
    }
}
