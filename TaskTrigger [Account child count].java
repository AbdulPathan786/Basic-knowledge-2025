If Task insert or update then Account record will be update and couns child record.
WhoId   Lookup(Contact,Lead)
WhatId Account
if((obj.WhatId != null) && (trigger.isInsert || (obj.WhatId != trigger.oldmap.get(l.id).WhatId))){}
-------------------------------------------------------------------
trigger TaskTrigger on Task(After Insert, After Update){
    if(Trigger.isAfter && Trigger.isInsert){
        TaskTriggerHelper.onAfterInsert(trigger.new);
    }
    if(Trigger.isAfter && Trigger.isUpdate){
        TaskTriggerHelper.onAfterUpdate(trigger.new, trigger.oldMap);
    }
}
-
public class TaskTriggerHelper{
  public static void onAfterInsert(List<Task> taskRecords){
    Set<String> setAccountIds = new Set<String>();
    if(!taskRecords.isEmpty()){
      for(Task taskRecord : taskRecords){
        if(taskRecord.AccountId != null){
          setAccountIds.add(taskRecord.AccountId);
        }
      }
    }
    if(!setAccountIds.isEmpty()){
        updateAccountRecords(setAccountIds);
    } 
  }

  public static void onAfterUpdate(List<Task> taskRecords, Map<Id, Task> oldMap){
    Set<String> setAccountIds = new Set<String>();
    if(!taskRecords.isEmpty()){
        for(Task objTask: taskRecords){
          if(objTask.AccountId != null && oldMap !=null && oldMap.get(objTask.Id).AccountId != objTask.AccountId){
              setAccountIds(objTask.AccountId); 
          }
      }
    }
    if(!setAccountIds.isEmpty()){
        updateAccountRecords(setAccountIds);
    }
  }

  private static void updateAccountRecords(Set<String> setAccountIds){
    List<Account> updateAccountList = new List<Account>();
    List<Account> accountRecords = [SELECT Id, Total_Task__c, (SELECT Id FROM Tasks) FROM Account WHERE AccountId IN: setAccountIds ];
    if(!accountRecords.isEmpty()){
      for(Account objAccount: accountRecords){
        if(objAccount.Tasks.size() > 0){
            objAccount.Total_Task__c = objAccount.Tasks.size();
            updateAccountList(objAccount);
        }
      }
    }
  }
  private static void updateAccountRecords2(Set<Id> accountIds) {
      List<Account> accountsToUpdate = new List<Account>();
      List<AggregateResult> taskCounts=[SELECT AccountId, COUNT(Id) taskCount FROM Task WHERE AccountId IN :accountIds GROUP BY AccountId];
      Map<Id, Integer> accountTaskCountMap = new Map<Id, Integer>();
      
      for (AggregateResult result : taskCounts) {
          accountTaskCountMap.put((Id)result.get('AccountId'), (Integer)result.get('taskCount'));
      }

      for (Id accId : accountIds) {
          Account accountToUpdate = new Account(Id = accId, Number_of_Tasks__c = accountTaskCountMap.get(accId));
          accountsToUpdate.add(accountToUpdate);
      }

      if (!accountsToUpdate.isEmpty()) {
          update accountsToUpdate;
      }
    }
}
-------------------------------END---------------------------------------------------