package com.bitcola.chain.proxy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class ScanPackage implements ResourceLoaderAware {

    @Autowired
    private ResourceLoader resourceLoader;

    public List<Class> readPackageClazz(String path) throws IOException {
        if(path.endsWith("/")) path=path.substring(0,path.length()-1);
        ResourcePatternResolver resolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
        MetadataReaderFactory metaReader = new CachingMetadataReaderFactory(resourceLoader);
        Resource[] resources = resolver.getResources(String.format("classpath*:%s/**/*.class",path));
        List<Class>classList=new ArrayList<>(30);
        for (Resource r : resources) {
            MetadataReader reader = metaReader.getMetadataReader(r);
            String p=reader.getClassMetadata().getClassName();
            try {
                Class clazz=  Class.forName(p);
                classList.add(clazz);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return classList;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
}
