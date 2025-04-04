Write a trigger that updates the Last_Contacted_Date__c field on the Account object whenever a Contact associated with that Account is updated.
 
  Map<Id, Date> accountIdDateMap=new Map<Id, Date>();
  for(Contact objContact: contactList){
      if(objContact.AccountId = Null){
          if(accountIdDateMap.containsKey(objContact.AccountId)){
              if(objContact.LastModifidate > accountIdDateMap.get(objContact.AccountId)){
                  accountIdDateMap.get(objContact.AccountId).add(objContact.LastModifidate);
              }
          }else{
              accountIdDateMap.put(objContact.AccountId, objContact.LastModifidate);
          }
      }
  }
  if(!accountIdDateMap.isEmpty()){
    for(Id accId: accountIdDateMap.keySet()){
        Account obj=new Account(Id=accId, Last_Contacted_Date__c=accountIdDateMap.get(accId));
    }
  } 