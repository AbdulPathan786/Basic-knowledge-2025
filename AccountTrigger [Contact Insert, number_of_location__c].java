Account insert then child records created according to account field values // number_of_location__c
-------------------------------------------------------------------------------
trigger AccountTrigger on Account (after insert) {
    if (Trigger.isInsert && Trigger.isAfter) {
        AccountTriggerHelper.onAfterInsert(Trigger.new);
    }
}
---------
public class AccountTriggerHelper {
  public static void onAfterInsert(List<Account> accountRecords){
    Map<Id, Integer> accountIdNumberOfLocationMap=new Map<Id, Integer>();
    List<Contact> contactRecords = new List<Contact>();

    for(Account objAccount: accountRecords){
      if(objAccount.number_of_location__c != null && objAccount.number_of_location__c >0){
          accountIdNumberOfLocationMap.put(objAccount.Id, objAccount.number_of_location__c);
      }
    }

    for(Id accountId : accountIdNumberOfLocationMap.keySet()){
      Integer numberOfLocations = accountIdNumberOfLocationMap.get(accountId);
      for(Integer i = 1; i <= numberOfLocations; i++){
        Contact objContact = new Contact();
        objContact.AccountId = accountId;
        objContact.LastName = 'Test' + i;
        contactRecords.add(objContact);
      }
    }
    if (!contactRecords.isEmpty()) {
     Insert contactRecords;  
    }
  }
}
-------------------------------END---------------------------------------------------