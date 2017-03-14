package org.limlee.extra;

import org.limlee.annotation.Extra;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;

public class ExtraModuleAnnotatedClass {
    private Set<ExtraAnnotatedClass> mExtraAnnotatedClasses = new HashSet<>();
    private Set<ExtraAnnotatedClass> mVaildExtraAnnotatedClasses = new HashSet<>();
    private Map<String, ExecutableElement> mSetterMethods = new HashMap();
    TypeElement mTypeElement;
    String mClassName;
    String mClassType;
    String mPackageName;

    public ExtraModuleAnnotatedClass(TypeElement element) {
        mTypeElement = element;
        mClassName = mTypeElement.getQualifiedName().toString();
        mClassType = mTypeElement.getSimpleName().toString();
        List<Element> enclosingElementList = (List<Element>) mTypeElement.getEnclosedElements();
        if (null != enclosingElementList) {
            for (Element enclosingElementItem : enclosingElementList) {
                if (enclosingElementItem.getKind() == ElementKind.FIELD
                        && !ProcessorHelper.isFinalElement(element)
                        && !ProcessorHelper.isStaticElement(element)
                        && !ProcessorHelper.isNativeElement(element)
                        ) {
                    Extra extraAnnotation = enclosingElementItem.getAnnotation(Extra.class);
                    if (null != extraAnnotation) {
                        ExtraAnnotatedClass extraAnnotatedClass = new ExtraAnnotatedClass((VariableElement) enclosingElementItem);
                        if (!extraAnnotatedClass.mIsPrivate
                                || null != findVisibleSetterElement(extraAnnotatedClass)) {
                            mExtraAnnotatedClasses.add(extraAnnotatedClass);
                        }
                    }
                }
            }
        }
    }

    private ExecutableElement findVisibleSetterElement(ExtraAnnotatedClass extraAnnotatedClass) {
        final String filedName = extraAnnotatedClass.getFiledName();
        ExecutableElement setterElement = mSetterMethods.get(filedName);
        if (null == setterElement) {
            String methodName = filedName.length() == 1 ? filedName.toUpperCase()
                    : Character.toUpperCase(filedName.charAt(0)) + filedName.substring(1);
            methodName = String.format("set%s", methodName); //只支持setAbc(D e)这样的方法
            List<Element> enclosingElementList = (List<Element>) mTypeElement.getEnclosedElements();
            if (null != enclosingElementList) {
                for (Element element : enclosingElementList) {
                    if (element.getKind() == ElementKind.METHOD
                            && !ProcessorHelper.isPrivateElement(element)) {
                        if (element.getSimpleName().toString().equals(methodName)
                                && isVaildSetterMethod(extraAnnotatedClass, (ExecutableElement) element)) {
                            mSetterMethods.put(filedName, (ExecutableElement) element);
                            return (ExecutableElement) element;
                        }
                    }
                }
            }
        }
        return setterElement;
    }

    private boolean isVaildSetterMethod(ExtraAnnotatedClass extraAnnotatedClass, ExecutableElement setterMethod) {
        List<? extends VariableElement> parameters = setterMethod.getParameters();
        if (null != parameters
                && parameters.size() == 1) {
            VariableElement variableElement = parameters.get(0);
            if (null != variableElement
                    && variableElement.asType().equals(extraAnnotatedClass.mType)) {
                return true;
            }
        }
        return false;
    }

    public void genCode(ProcessingEnvironment processingEnv) {
        genExtraCode(processingEnv);
        genInjectorCode(processingEnv);
    }

    //template
//    public static class PreRecorderFragment_Extra {
//
//        private Bundle bundle;
//
//        public PreRecorderFragment_Extra videoId(String videoId) {
//            bundle.putString("videoId", videoId);
//            return this;
//        }
//
//        public PreRecorderFragment_Extra pushUrl(String pushUrl) {
//            bundle.putString("pushUrl", pushUrl);
//            return this;
//        }
//
//        public Bundle get() {
//            return bundle;
//        }
//    }

    private void genExtraCode(ProcessingEnvironment processingEnv) {
        if (null != mPackageName
                && mPackageName.length() > 0
                && null != mClassName
                && mClassName.length() > 0
                && !mExtraAnnotatedClasses.isEmpty()) {
            JavaWriter javaWriter = null;
            try {
                final String className = mClassName + "Extra";
                final String classType = mClassType + "Extra";
                JavaFileObject javaFileObject = processingEnv.getFiler().createSourceFile(className);
                Writer writer = javaFileObject.openWriter();
                javaWriter = new JavaWriter(writer);
                javaWriter.emitPackage(mPackageName);
                javaWriter.emitImports("android.os.Bundle");
                javaWriter.emitEmptyLine();
                javaWriter.emitEmptyLine();
                javaWriter.beginType(className, "class", EnumSet.of(Modifier.PUBLIC, Modifier.FINAL))
                        .emitField("Bundle", "bundle", EnumSet.of(Modifier.PRIVATE), "new Bundle()")
                        .emitEmptyLine()
                        .beginMethod("Bundle", "get", EnumSet.of(Modifier.PUBLIC))
                        .emitStatement("return bundle")
                        .endMethod();
                for (ExtraAnnotatedClass extraAnnotatedClass : mExtraAnnotatedClasses) {
                    String bundlemethodTypeStr = getBundleMeothodType(processingEnv, extraAnnotatedClass.mVarElement);
                    if (bundlemethodTypeStr.length() > 0) {
                        mVaildExtraAnnotatedClasses.add(extraAnnotatedClass);
                        javaWriter.emitEmptyLine();
                        javaWriter.beginMethod(classType, extraAnnotatedClass.getFiledName(),
                                EnumSet.of(Modifier.PUBLIC), extraAnnotatedClass.mType.toString(), extraAnnotatedClass.getFiledName());
                        javaWriter.emitStatement("bundle.put%s(\"%s\", %s)", bundlemethodTypeStr, extraAnnotatedClass.mFileKeyName, extraAnnotatedClass.getFiledName());
                        javaWriter.emitStatement("return this");
                        javaWriter.endMethod();
                    }
                }
                javaWriter.endType();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (null != javaWriter) {
                        javaWriter.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected String getBundleMeothodType(ProcessingEnvironment processingEnv, Element element) {
        final String fieldTypeStr = element.asType().toString();
        //所支持的类型，比较常用的，如果没有你要支持的，请手动添加
        if (String.class.getCanonicalName().equals(fieldTypeStr)) { //String.class
            return "String";
        } else if (int.class.getCanonicalName().equals(fieldTypeStr)) { // int.class
            return "Int";
        } else if (boolean.class.getCanonicalName().equals(fieldTypeStr)) { //boolean.class
            return "Boolean";
        } else if (float.class.getCanonicalName().equals(fieldTypeStr)) { //float.class
            return "Float";
        } else if (double.class.getCanonicalName().equals(fieldTypeStr)) { //double.class
            return "Double";
        } else if (long.class.getCanonicalName().equals(fieldTypeStr)) { //long.class
            return "Long";
        } else if (isSerializableType(processingEnv, element)) { //Serializable.class
            return "Serializable";
        } else {
            return "";
        }
    }

    private boolean isSerializableType(ProcessingEnvironment processingEnv, Element element) {
        final String serialIzableType = Serializable.class.getCanonicalName();
        boolean isSerializeableType = false;
        TypeElement typeElement = processingEnv.getElementUtils().getTypeElement(element.asType().toString());
        List<? extends TypeMirror> interfaceTypeMirrors = typeElement.getInterfaces();
        if (null != interfaceTypeMirrors
                && !interfaceTypeMirrors.isEmpty()) {
            for (TypeMirror interfaceType : interfaceTypeMirrors) {
                if (interfaceType.toString().equals(serialIzableType)) {
                    isSerializeableType = true;
                    break;
                }
            }
        }
        if (!isSerializeableType) { //尝试去父类查找
            TypeMirror superTypeMirror = typeElement.getSuperclass();
            if (superTypeMirror.getKind() != TypeKind.NONE) {
                TypeElement superTypeElement = processingEnv.getElementUtils().getTypeElement(superTypeMirror.toString());
                return isSerializableType(processingEnv, superTypeElement);
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    //template
    //    public static class PreRecorderFragment_Injector implements ExtraInjector<PreRecorderFragment> {
//        private static final String EXTRA_VIDEOID = "videoId";
//        private static final String EXTRA_PUSHURL = "pushUrl";
//
//        @Override
//        public void inject(PreRecorderFragment target) {
//            if (null != target
//                    && null != target.getArguments()) {
//                final Bundle bundle = target.getArguments();
//                if (bundle.containsKey(EXTRA_VIDEOID)) {
//                    target.mVideoId = bundle.getString(EXTRA_VIDEOID);
//                }
//                if (bundle.containsKey(EXTRA_PUSHURL)) {
//                    target.mPushUrl = bundle.getString(EXTRA_PUSHURL);
//                }
//            }
//        }
//    }

    private void genInjectorCode(ProcessingEnvironment processingEnv) {
        if (null != mPackageName
                && mPackageName.length() > 0
                && null != mClassName
                && mClassName.length() > 0
                && !mVaildExtraAnnotatedClasses.isEmpty()) {
            JavaWriter javaWriter = null;
            try {
                final String className = mClassName + "Injector";
                final String classType = mClassType + "Injector";
                JavaFileObject javaFileObject = processingEnv.getFiler().createSourceFile(className);
                Writer writer = javaFileObject.openWriter();
                javaWriter = new JavaWriter(writer);
                javaWriter.emitPackage(mPackageName);
                javaWriter.emitImports("org.limlee.annotation.ExtraInjector");
                javaWriter.emitImports("android.os.Bundle");
                javaWriter.emitEmptyLine();
                javaWriter.beginType(className, "class", EnumSet.of(Modifier.PUBLIC, Modifier.FINAL)
                        , null, String.format("ExtraInjector<%s>", mClassType));

                javaWriter.emitEmptyLine();
                javaWriter.emitAnnotation("Override");
                javaWriter.beginMethod("void", "inject", EnumSet.of(Modifier.PUBLIC), mClassType, "target");
                javaWriter.beginControlFlow("if(null != target && null != target.getArguments())");
                javaWriter.emitStatement("final Bundle bundle = target.getArguments()");
                for (ExtraAnnotatedClass extraAnnotatedClass : mVaildExtraAnnotatedClasses) {
                    String bundlemethodTypeStr = getBundleMeothodType(processingEnv, extraAnnotatedClass.mVarElement);
                    javaWriter.emitEmptyLine();
                    javaWriter.beginControlFlow("if(bundle.containsKey(\"%s\"))", extraAnnotatedClass.mFileKeyName);
                    if (!extraAnnotatedClass.mIsPrivate) {
                        if (bundlemethodTypeStr.equals("Serializable")) {
                            javaWriter.emitStatement("target.%s = (%s)bundle.get%s(\"%s\")",
                                    extraAnnotatedClass.mFiledName, extraAnnotatedClass.mType.toString(), bundlemethodTypeStr, extraAnnotatedClass.mFileKeyName);
                        } else {
                            javaWriter.emitStatement("target.%s = bundle.get%s(\"%s\")",
                                    extraAnnotatedClass.mFiledName, bundlemethodTypeStr, extraAnnotatedClass.mFileKeyName);
                        }
                    } else {
                        ExecutableElement setterMethodElement = mSetterMethods.get(extraAnnotatedClass.getFiledName());
                        if (null != setterMethodElement) {
                            if (bundlemethodTypeStr.equals("Serializable")) {
                                javaWriter.emitStatement("target.%s((%s)bundle.get%s(\"%s\"))",
                                        setterMethodElement.getSimpleName().toString(), extraAnnotatedClass.mType.toString(), bundlemethodTypeStr, extraAnnotatedClass.mFileKeyName);
                            } else {
                                javaWriter.emitStatement("target.%s(bundle.get%s(\"%s\"))",
                                        setterMethodElement.getSimpleName().toString(), bundlemethodTypeStr, extraAnnotatedClass.mFileKeyName);
                            }
                        }
                    }
                    javaWriter.endControlFlow();
                }

                javaWriter.endControlFlow();
                javaWriter.endMethod();

                javaWriter.endType();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (null != javaWriter) {
                        javaWriter.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public int hashCode() {
        return mClassName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (null != obj
                && obj instanceof ExtraModuleAnnotatedClass
                && ((ExtraModuleAnnotatedClass) obj).mClassName.equals(this.mClassName)) {
            return true;
        }
        return false;
    }
}
