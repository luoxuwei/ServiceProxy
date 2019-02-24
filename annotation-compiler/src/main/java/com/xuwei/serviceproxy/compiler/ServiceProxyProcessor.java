package com.xuwei.serviceproxy.compiler;

import com.google.auto.service.AutoService;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import com.xuwei.serviceproxy.annotation.ProxyModule;
import com.xuwei.serviceproxy.compiler.utils.Logger;

/**
 * Created by xuwei.luo on 18/8/31.
 */
@AutoService(Processor.class)
public class ServiceProxyProcessor extends AbstractProcessor {
    private Filer mFiler;       // File util, write class file into disk.
    private Elements mElements;
    private Types mTypes;
    private Logger mLogger;
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        mFiler = processingEnv.getFiler();                  // Generate class.
        mTypes = processingEnv.getTypeUtils();            // Get type utils.
        mElements = processingEnv.getElementUtils();      // Get class meta.
        mLogger = new Logger(processingEnv.getMessager());   // Package the log utils.
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Set<? extends Element> proxyAnnos = roundEnvironment.getElementsAnnotatedWith(ProxyModule.class);
        if (proxyAnnos != null) {
            mLogger.info(">>> Found Proxy, start... <<<");
            mLogger.info(">>> Found Proxy, size is " + proxyAnnos.size() + " <<<");
            for (Element element : proxyAnnos) {
                if (element.getKind() == ElementKind.CLASS && element instanceof TypeElement) {
                    ClassProxyBuilder builder = ClassProxyBuilder.builder((TypeElement) element, mElements, mTypes, mLogger);
                    try {
                        builder.buildProxyClass().writeTo(mFiler);
                    } catch (IOException e) {
                        mLogger.error(e);
                    }
                }
            }
        }
        return false;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> supportedAnnotatiion = new LinkedHashSet<>();
        supportedAnnotatiion.add(ProxyModule.class.getCanonicalName());
        return supportedAnnotatiion;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
