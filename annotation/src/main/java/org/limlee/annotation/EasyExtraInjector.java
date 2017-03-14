package org.limlee.annotation;


public class EasyExtraInjector {

    public static void inject(Object obj) {
        try {
            final String injectTargetName = obj.getClass().getCanonicalName() + "Injector";
            final Class<?> injecTargetClass = Class.forName(injectTargetName);
            Object injecTargetObj = injecTargetClass.newInstance();
            if (injecTargetObj instanceof ExtraInjector) {
                ((ExtraInjector) injecTargetObj).inject(obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
