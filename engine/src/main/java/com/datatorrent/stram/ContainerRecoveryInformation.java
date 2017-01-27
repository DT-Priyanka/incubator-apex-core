package com.datatorrent.stram;

import java.util.Set;

import com.datatorrent.stram.plan.physical.PTOperator;

public class ContainerRecoveryInformation
{
  String inactiveContainerId;
  Set<PTOperator> OpeartorsToStop;
  Set<PTOperator> OpeartorsToStart;

  public ContainerRecoveryInformation(String inactiveContainerId, Set<PTOperator> opeartorsToStop,
      Set<PTOperator> opeartorsToStart)
  {
    this.inactiveContainerId = inactiveContainerId;
    OpeartorsToStop = opeartorsToStop;
    OpeartorsToStart = opeartorsToStart;
  }

  public String getInactiveContainerId()
  {
    return inactiveContainerId;
  }

  public void setInactiveContainerId(String inactiveContainerId)
  {
    this.inactiveContainerId = inactiveContainerId;
  }

  public Set<PTOperator> getOpeartorsToStop()
  {
    return OpeartorsToStop;
  }

  public void setOpeartorsToStop(Set<PTOperator> opeartorsToStop)
  {
    OpeartorsToStop = opeartorsToStop;
  }

  public Set<PTOperator> getOpeartorsToStart()
  {
    return OpeartorsToStart;
  }

  public void setOpeartorsToStart(Set<PTOperator> opeartorsToStart)
  {
    OpeartorsToStart = opeartorsToStart;
  }

}
