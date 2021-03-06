package me.tatarka.jackport;

import com.android.jack.Jack;
import com.android.jack.plugin.v01.SchedAnnotationProcessorBasedPlugin;
import com.android.sched.item.Component;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.scheduler.FeatureSet;
import com.android.sched.scheduler.ProductionSet;
import com.android.sched.scheduler.Scheduler;
import com.android.sched.util.SubReleaseKind;
import com.android.sched.util.Version;
import com.android.sched.util.config.Config;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

public class Base extends SchedAnnotationProcessorBasedPlugin {

    @Nonnull
    @Override
    public String getCanonicalName() {
        return Base.class.getCanonicalName();
    }

    @Nonnull
    @Override
    public String getFriendlyName() {
        return "Backport Base Java8 api";
    }

    @Nonnull
    @Override
    public String getDescription() {
        return "Backport Base Java8 api";
    }

    @Nonnull
    @Override
    public Version getVersion() {
        return new Version("jackport-java8-base", "1.0", 1, 0, SubReleaseKind.ENGINEERING);
    }

    @Override
    public boolean isCompatibileWithJack(@Nonnull Version version) {
        return true;
    }

    @Nonnull
    @Override
    public FeatureSet getFeatures(@Nonnull Config config, @Nonnull Scheduler scheduler) {
        FeatureSet featureSet = scheduler.createFeatureSet();
        featureSet.add(BackportJava8Api.class);
        return featureSet;
    }

    @Nonnull
    @Override
    public ProductionSet getProductions(@Nonnull Config config, @Nonnull Scheduler scheduler) {
        return scheduler.createProductionSet();
    }

    @Nonnull
    @Override
    public List<Class<? extends RunnableSchedulable<? extends Component>>> getSortedRunners() {
        return Collections.<Class<? extends RunnableSchedulable<? extends Component>>>
                singletonList(ChangePackageConverter.class);
    }

    @Nonnull
    @Override
    public Collection<Class<? extends RunnableSchedulable<? extends Component>>> getCheckerRunners() {
        return Collections.emptyList();
    }
}
