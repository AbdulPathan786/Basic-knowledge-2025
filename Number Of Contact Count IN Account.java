Number Of Contact Count IN Account
Contact -> INSERT UPDATE DELETE UNDELETE
Account Number_Contact => 
-----------------------------------------------------------------------------------------
Trigger on ContactTrigger Contact(After Insert, After Update , After Delete){
	Switch on Trigger.operationType{
		WHEN AFTER_INSERT{
			ContactTriggerHelper.onAfterInsert(Trigger.new);
		}
		WHEN AFTER_UPDATE{
			ContactTriggerHelper.onAfterUpdate(Trigger.new, Trrigger.oldMap)
		}
		WHEN AFTER_DELETE{
			ContactTriggerHelper.onAfterDelete(Trigger.old)
		}
		WHEN AFTER_UNDELETE{
			ContactTriggerHelper.onAfterUnDelete(Trigger.new)
		}
	}
}
---------Helper class------------------------------------------------------------
public class ContactTriggerHelper{
	public Static void onAfterInsert(List<Contact> contactRecords){
		Set<String> setAccountId=new Set<String>();

		if(!contactRecords.isEmpty()){
			For(Contact objContact: contactRecords){
				if(objContact.AccountId != null){
					setAccountId.add(objContact.AccountId);
				}
			}
		}
		if(!setAccountId.isEmpty()){
			AccountUpdate(setAccountId, contactRecords);
		}
	}
	
	public Static void onAfterUpdate(List<Contact> contactRecords, Map<Id, Contact> oldMap){
		Set<String> setAccountId=new Set<String>();

		if(!contactRecords.isEmpty()){
			For(Contact objContact: contactRecords){
				if(objContact.AccountId != null && oldMap != null && oldMap.get(objContact.AccountId) != objContact.AccountId){
					setAccountId.add(objContact.AccountId);
				}
			}
		}
		if(!setAccountId.isEmpty()){
			AccountUpdate(setAccountId);
		}
	}

	public static void onAfterDelete(List<Contact> contactRecords){
		Set<String> setAccountId=new Set<String>();

		if(!contactRecords.isEmpty()){
			For(Contact objContact: contactRecords){
				if(objContact.AccountId != null){
					setAccountId.add(objContact.AccountId);
				}
			}
		}
		if(!setAccountId.isEmpty()){
			AccountUpdate(setAccountId);
		}
	}
}

private Static void AccountUpdate(Set<String> setAccountId){
	List<Account> updateAccountRecords=new List<Account>();

	Map<String, Integer> accountIdAndTotalContactMap=new Map<String, Integer>();

	AggregateResult[] result=[SELECT AccountId, Count(Id) totalCount FROM Contact WHERE AccountId IN: setAccountId Group By AccountId];

	for(AggregateResult res : result){
		String accId = (Id)res.get('AccountId');
		Integer totalContact = (Integer)res.get('totalCount') != null : (Integer)res.get('totalCount') : 0;
		accountIdAndTotalContactMap.put(accId, totalContact);
	}


	if(!accountIdAndTotalContactMap.isEmpty()){
		for(Id accId: setAccountId){
			if(accountIdAndTotalContactMap.ContainsKey(accId)){
				Account objAccount=new Account(Id=accId, Number_Contact__c = accountIdAndTotalContactMap.get(accId));
				updateAccountRecords.add(objAccount);
			}
			
		}
	}

	if(!updateAccountRecords.isEmpty()){
		UPDATE updateAccountRecords;
	}
}