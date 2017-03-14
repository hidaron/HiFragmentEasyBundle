package org.limlee.extra;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.type.TypeMirror;

public class ProcessorHelper {

    public static boolean isFragmentClassElement(ProcessingEnvironment env, Element element) {
        if (null == element
                || element.getKind() != ElementKind.CLASS) {
            return false;
        }
        try {
            final TypeMirror fragmentType = env.getElementUtils().getTypeElement("android.app.Fragment").asType();
            final TypeMirror supportFragmentType =
                    env.getElementUtils().getTypeElement("android.support.v4.app.Fragment").asType();
            return
                    env.getTypeUtils().isSubtype(element.asType(), fragmentType)
                            || env.getTypeUtils().isSubtype(element.asType(), supportFragmentType);
        } catch (NullPointerException e) {
            return false;
        }
    }

    public static boolean isActivityClassElement(ProcessingEnvironment env, Element element) {
        if (null == element
                || element.getKind() != ElementKind.CLASS) {
            return false;
        }
        try {
            final TypeMirror activityType = env.getElementUtils().getTypeElement("android.app.Activity").asType();
            return env.getTypeUtils().isSubtype(element.asType(), activityType);
        } catch (NullPointerException e) {
            return false;
        }
    }

    public static String getPackageOfElement(ProcessingEnvironment env, Element element) {
        PackageElement packageElement = env.getElementUtils().getPackageOf(element);
        return !packageElement.isUnnamed() ? packageElement.getQualifiedName().toString() : "";
    }

    public static boolean isFinalElement(Element element) {
        return element.getModifiers().contains(Modifier.FINAL);
    }

    public static boolean isStaticElement(Element element) {
        return element.getModifiers().contains(Modifier.STATIC);
    }

    public static boolean isAbstractElement(Element element) {
        return element.getModifiers().contains(Modifier.ABSTRACT);
    }

    public static boolean isPrivateElement(Element element) {
        return element.getModifiers().contains(Modifier.PRIVATE);
    }

    public static boolean isProtectElement(Element element) {
        return element.getModifiers().contains(Modifier.PROTECTED);
    }

    public static boolean isNativeElement(Element element) {
        return element.getModifiers().contains(Modifier.NATIVE);
    }

}
