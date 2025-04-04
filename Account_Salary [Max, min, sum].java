"Following schema is provided":
Account
Total_Salary__c (Number)
Max_Salary__c (Number)

Account_Salary__c
Account__c (lookup)
Name (String)
Salary__c (Number)

An Account can have multiple Account_Salary__c records that look up to an Account by the Account__c.
Write a trigger that would update the Account Total_Salary__c, Max_Salary__c,
when a new Account_salary__c record is: Inserted, Updated (Consider update Only when Salary__c field is updated) Deleted
--------------------------------------------------------------------------------------------------
trigger AccountSalaryTrigger on Account_Salary__c (After Insert, After Update, After Delete) {
    if(trigger.isAfter && trigger.isInsert){
        AccountSalaryTriggerHelper.onAfterInsert(trigger.new);
    }
    if(trigger.isAfter && trigger.isUpdate){
        AccountSalaryTriggerHelper.onAfterUpdate(trigger.new, trigger.oldMap);
    }
    if(trigger.isAfter && trigger.isDelete){
        AccountSalaryTriggerHelper.onAfterDelete(trigger.old);
    }
}
---
public class AccountSalaryTriggerHelper {
  public static void onAfterInsert(List<Account_Salary__c> accountSalaryRecords){
    Set<Id> accountIds=new Set<Id>();
    if(!accountSalaryRecords.isEmpty()){
      for(Account_Salary__c accSalary: accountSalaryRecords){
        if(accSalary.Salary__c != null && accSalary.Account__c != null){
            accountIds.add(accSalary.Account__c);
         }
      }
    }
    if(!accountIds.isEmpty()){
      updateAccountRecords(accountIds);
    }
  }
---
public static void onAfterUpdate(List<Account_Salary__c> accountSalaryRecords, Map<Id, Account_Salary__c> oldMap){
  Set<Id> accountIds=new Set<Id>();
  if(!accountSalaryRecords.isEmpty()){
    for(Account_Salary__c accSalary: accountSalaryRecords){
      if(accSalary.Account__c != null && oldMap != null && (oldMap.get(accSalary.Id).Salary__c != accSalary.Salary__c || 
          oldMap.get(accSalary.Id).Account__c != accSalary.Account__c){
          accountIds.add(accSalary.Account__c);
      }
    }
    if(!accountIds.isEmpty()){
       updateAccountRecords(accountIds);
    }
  }
}
---
public static void onAfterDelete(List<Account_Salary__c> accountSalaryRecords){
  Set<Id> accountIds=new Set<Id>();
  if(!accountSalaryRecords.isEmpty()){
    for(Account_Salary__c accSalary: accountSalaryRecords){
     if(accSalary.Salary__c != null && accSalary.Account__c != null){
         accountIds.add(accSalary.Account__c);
      }
    }
    if(!accountIds.isEmpty()){
       updateAccountRecords(accountIds);
    }
  }
}
---
private static void updateAccountRecords(Set<Id> accIds){
 List<Account> updateAccountList = new List<Account>();
 if(!accIds.isEmpty()){
  AggregateResult[] results = [SELECT Account__c, Sum(Salary__c) totalSum, Max(Salary__c) maxSalary, Min(Salary__c) minSalary 
                                FROM Account_Salary__c 
                                WHERE Account__c IN :accIds  GROUP BY Account__c
                              ];
  if(results != null) {
    for(AggregateResult ar: results){
      Id accountId = (Id) ar.get('Account__c');
      Decimal totalSalary = (Decimal) ar.get('totalSalary');
      Decimal maxSalary = (Decimal) ar.get('maxSalary');
      Decimal minSalary = (Decimal) ar.get('minSalary');

      Account acc = new Account(Id = accountId, Total_Salary__c = totalSalary, Max_Salary__c = maxSalary);
      updateAccountList.add(acc);
    }
  } 
} 
---------------------------------------------------------------
public static void updateAccountRecords2(List<Account_Salary__c> accountSalaryRecords){
  Map<Id, Decimal> accountTotalSalaryMap = new Map<Id, Decimal>();
  Map<Id, Decimal> accountMaxSalaryMap = new Map<Id, Decimal>();
  if(!accountSalaryRecords.isEmpty()){
    for(Account_Salary__c accSalary: accountSalaryRecords){
      if(accSalary.Salary__c != null && accSalary.Account__c != null){
        if(accountTotalSalaryMap.containsKey(accSalary.Account__c)){
          accountTotalSalaryMap.put(accSalary.Account__c , accountTotalSalaryMap.get(accSalary.Account__c) + accSalary.Salary__c);
        }else{
          accountTotalSalaryMap.put(accSalary.Account__c , accSalary.Salary__c);
        }
        if (accountMaxSalaryMap.containsKey(accSalary.Account__c)) { accountMaxSalaryMap.put(accSalary.Account__c, Math.max(accountMaxSalaryMap.get(accSalary.Account__c), accSalary.Salary__c));
        } else {
        accountMaxSalaryMap.put(accSalary.Account__c, accSalary.Salary__c);
        }
      }
    }
  }
  List<Account> accountsToUpdate = new List<Account>();
    for (Id accountId: accountTotalSalaryMap.keySet()) {
      Account acc = new Account(Id = accountId);
      // acc.Total_Salary__c = accountTotalSalaryMap.get(accountId);
      //acc.Max_Salary__c = accountMaxSalaryMap.get(accountId);
      accountsToUpdate.add(acc);
     }
     if (!accountsToUpdate.isEmpty()) {
        //update accountsToUpdate;
    }
  }
}
---------------------------END---------------------

// Set of Account Ids to be updated
Set<Id> accountIds = new Set<Id>();

// Collect Account Ids from inserted or updated Account_Salary__c records
if (Trigger.isInsert || Trigger.isUpdate || Trigger.isUndelete) {
    for (Account_Salary__c salary : Trigger.new) {
        accountIds.add(salary.Account__c);
    }
}

// Collect Account Ids from deleted Account_Salary__c records
if (Trigger.isDelete) {
    for (Account_Salary__c salary : Trigger.old) {
        accountIds.add(salary.Account__c);
    }
}
-----------------------
trigger AccountSalaryTrigger on Account_Salary__c (after insert, after update, after delete, after undelete) {

    // Set of Account Ids to be updated
    Set<Id> accountIds = new Set<Id>();

    // Collect Account Ids from inserted or updated Account_Salary__c records
    if (Trigger.isInsert || Trigger.isUpdate || Trigger.isUndelete) {
        for (Account_Salary__c salary : Trigger.new) {
            accountIds.add(salary.Account__c);
        }
    }

    // Collect Account Ids from deleted Account_Salary__c records
    if (Trigger.isDelete) {
        for (Account_Salary__c salary : Trigger.old) {
            accountIds.add(salary.Account__c);
        }
    }

    // Aggregate query to calculate total and max salary for each Account
    List<AggregateResult> aggregateResults = [
        SELECT Account__c,
               SUM(Salary__c) totalSalary,
               MAX(Salary__c) maxSalary
        FROM Account_Salary__c
        WHERE Account__c IN :accountIds
        GROUP BY Account__c
    ];

    // Map to store calculated salaries
    Map<Id, Account> accountsToUpdate = new Map<Id, Account>();

    // Process the aggregate results and prepare Account records for update
    for (AggregateResult ar : aggregateResults) {
        Id accountId = (Id) ar.get('Account__c');
        Decimal totalSalary = (Decimal) ar.get('totalSalary');
        Decimal maxSalary = (Decimal) ar.get('maxSalary');

        accountsToUpdate.put(accountId, new Account(
            Id = accountId,
            Total_Salary__c = totalSalary,
            Max_Salary__c = maxSalary
        ));
    }

    // Handle Accounts with no related Account_Salary__c records (set their salaries to 0)
    for (Id accountId : accountIds) {
        if (!accountsToUpdate.containsKey(accountId)) {
            accountsToUpdate.put(accountId, new Account(
                Id = accountId,
                Total_Salary__c = 0,
                Max_Salary__c = 0
            ));
        }
    }

    // Update the Accounts with the new salary values
    update accountsToUpdate.values();
}
----------