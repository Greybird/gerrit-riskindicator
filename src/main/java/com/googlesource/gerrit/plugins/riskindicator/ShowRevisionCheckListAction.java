package com.googlesource.gerrit.plugins.riskindicator;

import com.google.gerrit.extensions.restapi.RestModifyView;
import com.google.gerrit.extensions.webui.UiAction;
import com.google.gerrit.server.change.ChangeResource;
import com.google.gerrit.server.change.RevisionResource;
import com.google.inject.Inject;

public class ShowRevisionCheckListAction implements UiAction<ChangeResource>,
        RestModifyView<ChangeResource, ShowRevisionCheckListAction.Input> {
    static class Input {
    }

    @Inject
    ShowRevisionCheckListAction() {
    }

    @Override
    public String apply(ChangeResource rev, Input input) {
        return "";
    }

    @Override
    public Description getDescription(
            ChangeResource resource) {
        return new UiAction.Description()
                .setLabel("Review Check-List")
                .setTitle("Show Reviewer Check-List");
    }
}
