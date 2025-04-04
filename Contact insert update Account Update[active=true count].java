//Contact update then Account Update
trigger ContactTrigger on Contact (after insert, after update, after delete) {
    Set<Id> accountIdsToUpdate = new Set<Id>();
    
    if (Trigger.isInsert || Trigger.isUpdate) {
        for (Contact con : Trigger.new) {
            if (con.IsActive && con.AccountId != null) {
                accountIdsToUpdate.add(con.AccountId);
            }
        }
    }
    
    if (Trigger.isDelete) {
        for (Contact con : Trigger.old) {
            if (con.IsActive && con.AccountId != null) {
                accountIdsToUpdate.add(con.AccountId);
            }
        }
    }

    if (!accountIdsToUpdate.isEmpty()) {
        ContactTriggerHandler.updateActiveContactCount(accountIdsToUpdate);
    }
}
--
public class ContactTriggerHandler {
    public static void updateActiveContactCount(Set<Id> accountIds) {
        Map<Id, Integer> accountContactCountMap = new Map<Id, Integer>();
        List<Account> accountsToUpdate = new List<Account>();

        List<AggregateResult> aggregateResults = [SELECT AccountId, COUNT(Id) totalCount FROM Contact WHERE AccountId IN :accountIds AND IsActive = true GROUP BY AccountId ];

        for (AggregateResult ar : aggregateResults) {
            accountContactCountMap.put((Id) ar.get('AccountId'), (Integer) ar.get('totalCount'));
        }

        for (Id accountId : accountIds) {
            Integer activeContactCount = accountContactCountMap.get(accountId);
            accountsToUpdate.add(new Account( Id = accountId, Number_of_Active_Contacts__c = (activeContactCount != null) ? activeContactCount : 0 ));
        }

        if (!accountsToUpdate.isEmpty()) {
            update accountsToUpdate;
        }
    }
}
---------------------------END---------------------------------