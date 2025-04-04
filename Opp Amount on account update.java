----------------------START--------------------------------------------
Whenever Opportunity StageName field is updated to Closed Won then from Opportunity related account check and update sum
of total amount on account Total_Amount__c field 

onAfterInsert(Trigger.new);
onAfterUpdate(Trigger.new, Trigger.oldMap);
onAfterDelete(Trigger.old);
onAfterUnDelete(Trigger.new);
--INSERT----
public static void onAfterInsert(List<Oppotunity> opportunityList){
  Set<String> setAccontIds = new Set<String>();

  if(!opportunityList.isEmpty()){
    for(Opportunity objOpp: opportunityList){
      if(objOpp.AccountId != null && objOpp.Amount != null){
          setAccontIds.add(objOpp.AccountId);
      }
    }
  }
  if(!setAccontIds.isEmpty()){
      updateAccountRecord(setAccontIds)
  }
}
---UPDATE----
public static void onAfterUpdate(List<Oppotunity> opportunityList, Map<Id, Oppotunity> oldMap){
  Set<String> setAccontIds = new Set<String>();
  if(!opportunityList.isEmpty()){
    for(Opportunity objOpp: opportunityList){
      if(objOpp.AccountId != null && objOpp.Amount != null){
        if(oldMap !=null && oldMap.get(objOpp.Id).Amount != objOpp.Amount ){
            setAccontIds.add(objOpp.AccountId);
        }
      }
    }
  }
  if(!setAccontIds.isEmpty()){
    updateAccountRecord(setAccontIds)
  }
}
---DELETE-----
public static void onAfterDelete(List<Oppotunity> opportunityList){
  Set<String> setAccontIds = new Set<String>();
  if(!opportunityList.isEmpty()){
    for(Opportunity objOpp: opportunityList){
      if(objOpp.AccountId != null && objOpp.Amount != null){
          setAccontIds.add(objOpp.AccountId);       
      }
    }
  }
  if(!setAccontIds.isEmpty()){
      updateAccountRecord(setAccontIds)
  }
}
-------UNDELETE-------------
public static void onAfterUnDelete(List<Oppotunity> opportunityList){
  Set<String> setAccontIds = new Set<String>();
  if(!opportunityList.isEmpty()){
    for(Opportunity objOpp: opportunityList){
      if(objOpp.AccountId != null && objOpp.Amount != null){
          setAccontIds.add(objOpp.AccountId);       
      }
    }
  }
  if(!setAccontIds.isEmpty()){
    updateAccountRecord(setAccontIds)
  }
}
---COMMON------
public static void updateAccountRecord(Set<String> setAccontIds){
  List<Account> updateAccount=new List<Account>();

  List<AggregateResult> result = [SELECT AccountId, Sum(Amount) totalAmount FROM Oppotunity WHERE AccountId IN: setAccontIds Group By AccountId];
  For(AggregateResult res: result){
      Account obj=new Account(Id=(Id).res('AccountId'), Total_Amount__c=(Decimal).res('totalAmount') );
      updateAccount.add(obj);
  }
  if(!updateAccount.isEmpty()){
      UPDATE updateAccount;
  }
}
-------------------------------------------
trigger OpportunityTrigger on Opportunity(after insert, after update, after delete, after undelete){
    if(Trigger.isAfter){
      Set<Id> accIds = new Set<Id>();
      for(Opportunity opp: Trigger.new != null ? Trigger.new : Trigger.old){
          Opportunity oldOpp = Trigger.oldMap != null ? Trigger.oldMap.get(opp.Id) : null;
          if(opp.Amount != null && opp.AccountId != null && (Trigger.isDelete || oldOpp == Null || oldOpp.Amount != opp.Amount)){
              accIds.add(opp.AccountId);
          }
      }

      List<AggregateResult> aggs = [Select AccountId, SUM(Amount) From Opportunity Where AccountId IN: accIds GROUP By AccountId];
      List<Account> accounts = new List<Account>();
      for(AggregateResult ags : aggs){
          Id accountId = (Id)ags.get('AccountId');
          Double amount = (Double)ags.get('expr0');
          accounts.add(new Account(Id = accountId, Total_Amount__c = amount));
      }
      if(!accounts.isEmpty()){
          update accounts;
      }
    }
}
-------------------------END------------------------------------------------