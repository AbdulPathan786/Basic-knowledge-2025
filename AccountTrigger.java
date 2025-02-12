trigger AccountTrigger on Account (after insert, after update) {

    if (Trigger.isAfter) {
        switch on Trigger.operationType {
            when AFTER_INSERT, AFTER_UPDATE {
                AccountTriggerHelper.onAfterInsertUpdate(Trigger.new, Trigger.oldMap);
            }
        }
    }
}

public class AccountTriggerHelper {

    public static void onAfterInsertUpdate(List<Account> accountRecords, Map<Id, Account> oldMap) {
        Set<Id> setAccountIds = new Set<Id>();
        Map<Id, String> mapAccountIdAndName = new Map<Id, String>();
        List<Contact> insertContactRecords = new List<Contact>();

        // Query Contacts only if oldMap is not null (i.e., for AFTER_UPDATE operation)
        if (oldMap != null) {
            // Querying AccountId from Contact and adding it to the set
            for (Contact c : [SELECT AccountId FROM Contact WHERE AccountId IN :oldMap.keySet()]) {
                setAccountIds.add(c.AccountId);
            }
        }

        // Populate the map with Account Id and Name
        for (Account acc : accountRecords) {
            mapAccountIdAndName.put(acc.Id, acc.Name);
        }

        // Create new Contacts for Accounts that do not already have Contacts
        for (Id accId : mapAccountIdAndName.keySet()) {
            // Create new Contacts if no existing Contacts are linked to this Account, or if it's an INSERT operation
            if (!setAccountIds.contains(accId) || oldMap == null) {
                for (Integer i = 0; i < 5; i++) {
                    Contact newContact = new Contact(
                        AccountId = accId,
                        LastName = mapAccountIdAndName.get(accId) + ' ' + i
                    );
                    insertContactRecords.add(newContact);
                }
            }
        }

        // Insert new Contacts if there are any
        if (!insertContactRecords.isEmpty()) {
            insert insertContactRecords;  // Add the semicolon here
        }
    }
}
-----------------------SECOND CONDITION-----
trigger AccountTrigger on Account (after insert, after update) {

    // Ensure that the trigger runs only after the Account is updated (for field 'number__c')
    if (Trigger.isAfter) {
        switch on Trigger.operationType {
            when AFTER_INSERT, AFTER_UPDATE {
                AccountTrigger.onAfterInsertUpdate(Trigger.new, Trigger.oldMap);
            }
        }
    }
}

public class AccountTrigger {

    public static void onAfterInsertUpdate(List<Account> accountRecords, Map<Id, Account> oldMap) {
        List<Contact> contactInsert = new List<Contact>();
        Set<Id> accountIdsToCheck = new Set<Id>();
        Map<Id, Integer> mapAccountIdToContactCount = new Map<Id, Integer>();

        for (Account acc : accountRecords) {
            accountIdsToCheck.add(acc.Id);
        }

        for (AggregateResult ar : [SELECT AccountId, COUNT(Id) contactCount FROM Contact 
                                   WHERE AccountId IN :accountIdsToCheck 
                                   GROUP BY AccountId]) {
            mapAccountIdToContactCount.put((Id) ar.get('AccountId'), (Integer) ar.get('contactCount'));
        }

        // Iterate through the Accounts and compare the number of existing Contacts with 'number__c'
        for (Account acc : accountRecords) {
            Integer requiredContacts = acc.number__c;//5
            Integer existingContacts = mapAccountIdToContactCount.get(acc.Id) != null ? mapAccountIdToContactCount.get(acc.Id) : 0;

            // Only create new Contacts if the required number is greater than the existing number
            if (requiredContacts > existingContacts) {//10
                for (Integer i = existingContacts; i < requiredContacts; i++) {
                    Contact newContact = new Contact(
                        AccountId = acc.Id,
                        LastName = "Test" + (i + 1)
                    );
                    contactInsert.add(newContact);
                }
            }
        }

        // Insert new Contacts if there are any to insert
        if (!contactInsert.isEmpty()) {
            insert contactInsert;
        }
    }
}
