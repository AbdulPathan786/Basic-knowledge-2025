public class RefactorTest {
    // refactor the methods below to improve performance and readability
    public static final String VIP = 'VIP';
    public static final String SYSTEM_ADMINISTRATOR = 'System Administrator';
    
    public static void restrictChangeOwner(List<Case> caseRecords, Map<Id,Case> oldMap) {
        Map<Id,List<Case>> accountIdAndCaseListMap = new Map<Id,List<Case>>();
        Set<Id> setVipMemberAccountIds = new Set<Id>();
        Set<Id> userIdsWithVipAccess = new Set<Id>();
        
        if(!caseRecords.isEmpty()){
            for (Case objCase: caseRecords) {
                if(objCase.AccountId != null){
                    if(accountIdAndCaseListMap.containsKey(objCase.AccountId)){
                        accountIdAndCaseListMap.get(objCase.AccountId).add(obj);
                    }else{
                        accountIdAndCaseListMap.put(objCase.AccountId, new List<Case>{objCase});
                    }
                    if (objCase.OwnerID.getSobjectType() == User.SobjectType && objCase.OwnerId != oldMap.get(objCase.Id).OwnerId) {
                        userIdsWithVipAccess.put(objCase.OwnerId);
                    }
                }
            }
        }
        
        setVipMemberAccountIds = new Set<Id>([SELECT Id FROM Account WHERE  Id =: accountIdAndCaseListMap.keySet() AND Type=: VIP]);
        Map<Id, User> adminAccessMap = hasVIPAccess(userIdsWithVipAccess);
        
        if(!caseRecords.isEmpty()){
            for (Case caseRecord: caseRecords) {
                if(caseRecord.AccountId != null && setVipMemberAccountIds.contains(caseRecord.AccountId) && !adminAccessMap.containsKey(caseRecord.OwnerId)) {
                    caseRecord.addError('You do not have access to change the owner of this Case');
                }
            }
        }
    }
}

// only Sys Admins have VIP access
public static Map<Id, User> hasVIPAccess(Set<String> userIds) {
    Map<Id, User> adminUsers = new Map<Id, User>([SELECT Id  FROM User WHERE Id =: userIds AND  Profile.Name == SYSTEM_ADMINISTRATOR]);
    return adminUsers;
}