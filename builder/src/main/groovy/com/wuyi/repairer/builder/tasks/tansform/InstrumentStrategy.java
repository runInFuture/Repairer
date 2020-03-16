package com.wuyi.repairer.builder.tasks.tansform;

import com.android.build.api.variant.VariantInfo;

import org.gradle.api.Project;

public interface InstrumentStrategy {
    boolean apply(Project project, VariantInfo variantInfo);

    InstrumentStrategy RELEASE_ONLY = new InstrumentStrategy() {
        @Override
        public boolean apply(Project project, VariantInfo variantInfo) {
            return variantInfo.getBuildTypeName().equals("release");
        }
    };
}
