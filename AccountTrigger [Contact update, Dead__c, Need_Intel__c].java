When the account update then related to all contacts update []

trigger AccountTrigger on Account (after update) {
    if (Trigger.isAfter && Trigger.isUpdate) {
        AccountTriggerHelper.onAfterUpdate(Trigger.new, Trigger.oldMap);
    }
}
public class AccountTriggerHelper {
  public static void onAfterUpdate(List<Account> accountRecords, Map<Id, Account> oldMap) {
  Map<Id, Boolean> accoundIdandStatusMap = new Map<Id, Boolean>();
  List<Contact> updateContactRecord = new List<Contact>();

  // Collect updated accounts and their Need_Intel__c values
  for (Account obj: accountRecords) {
    if(obj.Need_Intel__c != null && oldMap != null && oldMap.get(obj.Id).Need_Intel__c != obj.Need_Intel__c) {
        accoundIdandStatusMap.put(obj.Id, obj.Need_Intel__c);
    } 
  }
  // Fetch related contacts based on the updated accounts
  if (!accoundIdandStatusMap.isEmpty()) {
    List<Contact> contactList = [SELECT Id, AccountId, Dead__c FROM Contact WHERE AccountId IN: accoundIdandStatusMap.keySet()];

    for(Contact obj: contactList) {
      if(accoundIdandStatusMap.containsKey(obj.AccountId)) {
          Contact obj=new Contact(Id=obj.Id, Dead__c=accoundIdandStatusMap.get(obj.AccountId));
          updateContactRecord.add(obj);
      }
    }
  }
  if (!updateContactRecord.isEmpty()){
     update updateContactRecord;
  }
}
-------------------