package com.datatorrent.stram;

import java.util.List;

import com.google.common.collect.Lists;

public class DeploymentGroupEvent
{
  RootCause rootCause; // this is an enum to indicate operator failure or container failure or other?
  long groupId;
  List<Integer> operatorsToStop = Lists.newArrayList();
  List<Integer> operatorsToStart = Lists.newArrayList();

  public static enum RootCause
  {
    OPERATOR_FAILURE, CONTAINER_FAILURE;
  }
}
