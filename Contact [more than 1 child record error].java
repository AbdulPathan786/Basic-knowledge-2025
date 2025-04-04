
When the contact is inserted or updated then check the account with more than 1 child record then the error shows.

trigger ContactTrigger Contact (before insert, before update) {
  if(trigger.isBefore && trigger.isInsert){
    ContactTriggerHelper.onBeforeInsert(trigger.new); }
  if(trigger.isBefore && trigger.isUpdate){
    ContactTriggerHelper.onBeforeUpdate(trigger.new, trigger.oldMap); 
  }
}
--
public class ContactTriggerHelper {
    public static void checkAccountContactLimit(List<Contact> newContacts) {
        Set<Id> accountIds = new Set<Id>();
        
        for (Contact con : newContacts) {
            if (con.AccountId != null) {
                accountIds.add(con.AccountId);
            }
        }
        
        if (!accountIds.isEmpty()) {
            AggregateResult[] results = [SELECT AccountId, COUNT(Id) contactCount FROM Contact
                                          WHERE AccountId IN :accountIds GROUP BY AccountId HAVING COUNT(Id) > 1
                                      ];
                                      
            Map<Id, Integer> accountContactCountMap = new Map<Id, Integer>();
            for (AggregateResult ar : results) {
                Id accountId = (Id) ar.get('AccountId');
                Integer contactCount = (Integer) ar.get('contactCount');
                accountContactCountMap.put(accountId, contactCount);
            }
            
            // Check if any of the new or updated contacts belong to an account with more than 1 contact
            for (Contact con : newContacts) {
                if (accountContactCountMap.containsKey(con.AccountId)) {
                    con.addError('An account cannot have more than one contact.');
                }
            }
        }
    }
}
--Second Method--
public class ContactTriggerHelper {
    public static void checkAccountContactLimit(Map<Id, Contact> newMap, Map<Id, Contact> oldMap) {
        Set<Id> accountIds = new Set<Id>();
        
        // Collect account IDs from the new or updated contacts
        for (Contact con : newMap.values()) {
            if (con.AccountId != null) {
                accountIds.add(con.AccountId);
            }
        }
        
        // Optionally, include AccountId from oldMap for updates if AccountId could change
        if (oldMap != null) {
            for (Contact con : oldMap.values()) {
                if (con.AccountId != null) {
                    accountIds.add(con.AccountId);
                }
            }
        }

        if (!accountIds.isEmpty()) {
            // Query the count of contacts grouped by account
            AggregateResult[] results = [
                SELECT AccountId, COUNT(Id) contactCount
                FROM Contact
                WHERE AccountId IN :accountIds
                GROUP BY AccountId
            ];
            
            Map<Id, Integer> accountContactCountMap = new Map<Id, Integer>();
            for (AggregateResult ar : results) {
                Id accountId = (Id) ar.get('AccountId');
                Integer contactCount = (Integer) ar.get('contactCount');
                accountContactCountMap.put(accountId, contactCount);
            }
            
            // Check if any of the new or updated contacts belong to an account with more than 1 contact
            for (Contact con : newMap.values()) {
                Integer contactCount = accountContactCountMap.get(con.AccountId);
                if (contactCount != null && contactCount > 1) {
                    con.addError('An account cannot have more than one contact.');
                }
            }
        }
    }
}
