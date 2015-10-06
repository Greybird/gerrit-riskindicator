package com.googlesource.gerrit.plugins.riskindicator;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Maps;
import com.google.gerrit.extensions.restapi.Response;
import com.google.gerrit.extensions.restapi.RestReadView;
import com.google.gerrit.reviewdb.client.Account;
import com.google.gerrit.reviewdb.client.Change;
import com.google.gerrit.reviewdb.client.PatchSet;
import com.google.gerrit.reviewdb.client.PatchSetApproval;
import com.google.gerrit.reviewdb.server.ReviewDb;
import com.google.gerrit.server.ApprovalsUtil;
import com.google.gerrit.server.IdentifiedUser;
import com.google.gerrit.server.change.ChangeResource;
import com.google.gerrit.server.change.ReviewerJson;
import com.google.gerrit.server.change.ReviewerResource;
import com.google.gerrit.server.change.RevisionResource;
import com.google.gerrit.server.notedb.ChangeNotes;
import com.google.gerrit.server.notedb.ReviewerState;
import com.google.gerrit.server.query.change.ChangeData;
import com.google.gwtorm.server.OrmException;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.eclipse.jgit.errors.NotSupportedException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

class RiskIndicator implements RestReadView<RevisionResource> {

  private final Provider<ReviewDb> dbProvider;
  private final ApprovalsUtil approvalsUtil;
  private final ReviewerResource.Factory resourceFactory;
  private final ChangeData.Factory changeDataFactory;

  @Inject
  RiskIndicator(Provider<ReviewDb> dbProvider,
                ApprovalsUtil approvalsUtil,
                ReviewerResource.Factory resourceFactory,
                ChangeData.Factory changeDataFactory) {
    this.dbProvider = dbProvider;
    this.approvalsUtil = approvalsUtil;
    this.resourceFactory = resourceFactory;
    this.changeDataFactory = changeDataFactory;
  }

  @Override
  public Response<Collection<RiskInfo>> apply(RevisionResource revision) {
    try {
      Collection<RiskInfo> l = new ArrayList<>();
      l.add(getRiskInfo("Change Risk", getChangeRisk(revision)));
      Collection<ReviewerResource> reviewers = getReviewers(revision);
      if (!reviewers.isEmpty()) {
        l.add(getRiskInfo("Review Risk", getApprovalLevel(reviewers)));
      }
      return Response.ok(l);
    }
    catch(Exception e)
    {
      return Response.none();
    }
  }

    private Collection<ReviewerResource> getReviewers(RevisionResource revision) throws OrmException {
      Map<Account.Id, ReviewerResource> reviewers = Maps.newLinkedHashMap();
      ReviewDb db = dbProvider.get();
      ChangeResource change = revision.getChangeResource();
      ImmutableSetMultimap<ReviewerState, Account.Id> categorizedReviewers = approvalsUtil.getReviewers(db, revision.getNotes());
      for (Account.Id accountId : categorizedReviewers.get(ReviewerState.REVIEWER)) {
        if (!reviewers.containsKey(accountId)) {
          reviewers.put(accountId, resourceFactory.create(change, accountId));
        }
      }
      return reviewers.values();
    }

  private int getApprovalLevel(Collection<ReviewerResource> reviewers) {
    int level = 2;
    for (ReviewerResource reviewer : reviewers) {
      int approvalLevel = getPersonLevel(reviewer.getUser());
      level = Math.min(level, approvalLevel);
    }
    return level;
  }

  private int getChangeRisk(RevisionResource revision) throws OrmException {
    int sizeLevel = getSizeLevel(revision);
    int personLevel = getPersonLevel(revision.getChangeResource(), revision.getChange().getOwner());
    return Math.max(sizeLevel, personLevel);
  }

  private int getPersonLevel(IdentifiedUser user) {
    String name = user.getName().toLowerCase();
    if (name.contains("expert")) {
      return 0;
    } else if (name.contains("junior"))  {
      return 2;
    }
    return 1;
  }


  private int getPersonLevel(ChangeResource change, Account.Id owner) {
    ReviewerResource committer =  resourceFactory.create(change, owner);
    return getPersonLevel(committer.getUser());
  }

  private int getSizeLevel(RevisionResource revision) throws OrmException {
    ReviewDb reviewDb = dbProvider.get();
    ChangeData cd = changeDataFactory.create(reviewDb, revision.getChange());
    ChangeData.ChangedLines changedLines = cd.changedLines();
    return getSizeLevel(Math.abs(changedLines.insertions - changedLines.deletions));
  }

  private int getSizeLevel(int difference) {
    if (difference > 200) {
      return 2;
    } else if (difference > 100) {
      return 1;
    }
    return 0;
  }

  private RiskInfo getRiskInfo(String name, int level)
  {
    String status;
    String image;
    switch(level)
    {
      case 0:
        status = "Low";
        image = "green";
        break;
      case 1:
        status = "Medium";
        image = "yellow";
        break;
      case 2:
        status = "High";
        image = "red";
        break;
      default:
        // pourvu que je n'aie pas de revue !
        throw new RuntimeException("PROUT");
    }
    return new RiskInfo(name, status, image);
  }

  class RiskInfo {
    String type;
    String level;
    String image;

    RiskInfo(String type, String level, String image) {
      this.type = type;
      this.level = level;
      this.image = "/plugins/riskindicator/static/" + image + ".png";
    }
  }
}
