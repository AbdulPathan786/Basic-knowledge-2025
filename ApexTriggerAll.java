------------------------------------------------------------------
Whenever Opportunity StageName field is updated to Closed Won then from Opportunity account check related contacts 
and set unique email ids comma seperated on Account custom field Named UniqueEmailIds__c
-------------------------------------------------------------------
account object amount __c custom fields 
then account insert then account parent update and total sum of amount in five level of account like
account, account.parent, account.parent.parent.... five lavel then five lavel account calcolate total amount
account update then the account parent field automatically update five level amounts sum
-------------------------------------------------------------------
---------------------START--------------------------
If creating contact then first check whether account exists or not, 
if yes then create corresponding contact otherwise first create account then create contact related to account

Map<Id, List<Contact>> accountIdContactList = new Map<Id, List<Contact>>();

for (Contact objContact : ContactList) {
    if (objContact.AccountId != null) {
        if (!accountIdContactList.containsKey(objContact.AccountId)) {
            accountIdContactList.put(objContact.AccountId, new List<Contact>{objContact});
        } else {
            accountIdContactList.get(objContact.AccountId).add(objContact);
        }
    }
}

if (!accountIdContactList.isEmpty()) {
    Set<Id> accountIds = new Set<Id>(accountIdContactList.keySet());
    Map<Id, String> existingAccountsMap = new Map<Id, String>();
  List<Contact> contactRecords = new List<Contact>();

    // Fetch existing accounts and store their names
    for (Account objAcc : [SELECT Id, Name FROM Account WHERE Id IN :accountIds]) {
        existingAccountsMap.put(objAcc.Id, objAcc.Name);
    }

    for (Id accId : accountIdContactList.keySet()) {
        List<Contact> contactsForAccount = accountIdContactList.get(accId);
        for (Contact objContact : contactsForAccount) {
            if (existingAccountsMap.containsKey(accId)) {
                // Existing Account
                Contact objCont = new Contact(
                    LastName = objContact.LastName,
                    AccountId = accId
                );
                contactRecords.add(objCont);
            } else {
                // New Account
                Account objAccount = new Account(Name = objContact.Account.Name);
                insert objAccount;

                Contact objCont = new Contact(
                    LastName = objContact.LastName,
                    AccountId = objAccount.Id
                );
                contactRecords.add(objCont);
            }
        }
    }

    if (!contactRecords.isEmpty()) {
        insert contactRecords;
    }
}
--------------------------END---------------------------------