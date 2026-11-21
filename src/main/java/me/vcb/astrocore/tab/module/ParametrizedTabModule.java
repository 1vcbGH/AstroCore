package me.vcb.astrocore.tab.module;

import lombok.Getter;
import me.vcb.astrocore.tab.PlayerTab;

@Getter
public abstract class ParametrizedTabModule<T> extends TabModule {

    public ParametrizedTabModule(String configSection) {
        super(configSection);
    }

    public abstract void apply(PlayerTab tab, T parameter);
}
