package com.googlesource.gerrit.plugins.riskindicator;

import static com.google.gerrit.server.change.RevisionResource.REVISION_KIND;
import static com.google.gerrit.server.change.ChangeResource.CHANGE_KIND;
import com.google.gerrit.extensions.restapi.RestApiModule;
import com.google.inject.AbstractModule;

public class Module extends AbstractModule {

  @Override
  protected void configure() {
    install(new RestApiModule() {
      @Override
      protected void configure() {
        get(REVISION_KIND, "riskindicator").to(RiskIndicator.class);
        post(CHANGE_KIND, "show-revision-checklist")
                .to(ShowRevisionCheckListAction.class);
      }
    });

  }
}
