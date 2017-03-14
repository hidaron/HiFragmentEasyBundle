package org.limlee.extra;


import org.limlee.annotation.Extra;
import org.limlee.annotation.ExtraModule;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class ExtraProcessor extends AbstractProcessor {
    private ProcessingEnvironment mProcessingEnv;
    private Filer mFiler;
    private Elements mElementUtils;
    private Types mTypesUtils;
    private Set<TypeElement> mExtraFragmentModules = new HashSet<>();


    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mElementUtils = processingEnv.getElementUtils();
        mFiler = processingEnv.getFiler();
        mTypesUtils = processingEnv.getTypeUtils();
        mProcessingEnv = processingEnv;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            for (Element element : roundEnv.getElementsAnnotatedWith(ExtraModule.class)) {
                if (ProcessorHelper.isFragmentClassElement(processingEnv, element)
                        && !ProcessorHelper.isAbstractElement(element)) {
                    mExtraFragmentModules.add((TypeElement) element);
                }
            }
            List<ExtraModuleAnnotatedClass> fragmentModuleClasses = new ArrayList<>();
            for (TypeElement fragmentModuleItem : mExtraFragmentModules) {
                ExtraModuleAnnotatedClass extraModuleAnnotatedClass = new ExtraModuleAnnotatedClass(fragmentModuleItem);
                extraModuleAnnotatedClass.mPackageName = ProcessorHelper.getPackageOfElement(mProcessingEnv, fragmentModuleItem);
                fragmentModuleClasses.add(extraModuleAnnotatedClass);
            }
            for (ExtraModuleAnnotatedClass fragmentModuleClassItem : fragmentModuleClasses) {
                fragmentModuleClassItem.genCode(mProcessingEnv);
            }
        } catch (Exception e) {
            printErrorMessage(e.getMessage());
        } finally {
            mExtraFragmentModules.clear();
        }
        return true;
    }


    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> supportTypes = new LinkedHashSet<>();
        supportTypes.add(Extra.class.getCanonicalName());
        supportTypes.add(ExtraModule.class.getCanonicalName());
        return supportTypes;
    }

    public void printErrorMessage(String message) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message);
    }

}
