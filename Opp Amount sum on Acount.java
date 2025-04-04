-------------------------------------------------------------------
Trigger OpportunityTrigger on Opportunity(After insert, after update){
  if(trigger.isInsert && trigger.isAfter){
    OpportunityTriggerHelper.onAfterInsert(trigger.new);
  }
}
--
public class OpportunityTriggerHelper{
  public static void onAfterInsert(List<Opportunity> opportunityList){
    Set<Id> setAccountId=new Set<Id>();
    Map<Id, Decimal> mapAccountIdAmount=new Map<Id, Decimal>();
    List<Account> updateAcountRecord=new List<Account>();
    
    if(!opportunityList.isEmpty()){
      for(Opportunity objOpp : opportunityList){
        if(objOpp.AccountId != null && objOpp.Amount != null){
          setAccountId(objOpp.AccountId);
        }
      }
    }
    if(!setAccountId.isEmpty()){
      List<AggregateResult> oppList = [SELECT AccountId, sum(Amount) totalAmount FROM Opportunity 
                                        WHERE AccountId IN: setAccountId Group by AccountId];
                        
      for(AggregateResult result: oppList){
          mapAccountIdAmount.put((Id)result.get('AccountId'), (Decimal)result.get('totalAmount'));
      }
    }
    
    if (!mapAccountIdAmount.isEmpty()) {
            for (Id accountId : mapAccountIdAmount.keySet()) {
                Account objAccount = new Account( Id = accountId, Amount__c = mapAccountIdAmount.get(accountId) );
                updateAccountRecord.add(objAccount);
            }
    }
    
    if(!mapAccountIdAmount.isEmpty()){
      for(Opportunity obj: opportunityList){
        if(mapAccountIdAmount.containsKey(obj.AccountId)){
          Account objAccount=new Account(Id =obj.Id, Amount__C = mapAccountIdAmount.get(obj.AccountId) );
          updateAcountRecord.add(objAccount);
        }
      }
    }
    if(!updateAcountRecord.isEmpty()){
      UPDATE updateAcountRecord;
    }
  }
}
--------------------END--------------------------------------