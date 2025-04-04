trigger AccountTrigger on Account (before delete) {
    if (Trigger.isBefore && Trigger.isDelete) {
        AccountTriggerHelper.onBeforeDelete(Trigger.old);
    }
}

public class AccountTriggerHelper {
    public static void onBeforeDelete(List<Account> accountRecords) {
        Set<Id> accountIds = new Set<Id>();

        for (Account account : accountRecords) {
            accountIds.add(account.Id);
        }

        if (!accountIds.isEmpty()) {
            List<Account> accountsWithContacts = [ SELECT Id, (SELECT Id FROM Contacts) FROM Account WHERE Id IN :accountIds ];

            for (Account account : accountsWithContacts) {
                if (account.Contacts.size() > 0) {
                    account.addError('Account associated with Contact records cannot be deleted.');
                }
            }
        }
    }
}
---------------------
trigger PreventAccountDeletion on Account (before delete) {
    Map<Id, Integer> accountContactCounts = new Map<Id, Integer>();
    Set<Id> accountIds = new Set<Id>();

    for (Account acc : Trigger.old) {
        accountIds.add(acc.Id);
    }

    for (AggregateResult ar : [SELECT AccountId, COUNT(Id) contactCount FROM Contact WHERE AccountId IN :accountIds GROUP BY AccountId ]){
        accountContactCounts.put((Id)ar.get('AccountId'), (Integer)ar.get('contactCount'));
    }

    for (Account acc : Trigger.old) {
        if (accountContactCounts.containsKey(acc.Id) && accountContactCounts.get(acc.Id) > 0) {
            acc.addError('Account cannot be deleted because it has one or more related Contacts.');
        }
    }
}
-------------------------