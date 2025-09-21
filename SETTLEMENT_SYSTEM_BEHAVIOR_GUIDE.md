# Settlement System Behavior Guide

## 🔐 **User Roles & Settlement Permissions**

### **PERSONAL Nex Type**

#### **Admin Users (Nex.ADMIN)**

- ✅ **Can settle the entire Nex**: Settle all debts in the personal Nex
- ✅ **Can settle individual debts**: Settle specific debt transactions
- ✅ **Can change settlement type**: Modify Nex settlement type
- ✅ **Full access**: All settlement operations

#### **Regular Users (Nex.MEMBER)**

- ❌ **Cannot settle entire Nex**: Cannot use `settleAll = true`
- ✅ **Can settle their own debts**: Only debts where they are debtor or creditor
- ❌ **Cannot change settlement type**: Only admins can modify Nex settings
- ✅ **Limited access**: Only personal debt settlements

### **GROUP Nex Type**

#### **All Users (Both Admin and Member)**

- ✅ **Can settle the entire Nex**: Settle all debts in the group
- ✅ **Can settle individual debts**: Settle specific debt transactions
- ✅ **Can see all settlements**: View all group settlements
- ✅ **Group behavior**: Everyone has group-level access

---

## 🏠 **Nex Type Behavior**

### **PERSONAL Nex Type**

**Individual-focused settlement management**

- **Settlement Visibility**: Users see only their own settlements (where they are debtor or creditor)
- **Settlement Execution**: Users can only settle their own debts
- **Admin Override**: Admins can still settle all debts and see all settlements
- **Use Case**: Personal expense tracking, individual debt management

**Example:**

```
PERSONAL Nex with Alice, Bob, Charlie
- Alice sees: Only settlements where she is debtor or creditor
- Bob sees: Only settlements where he is debtor or creditor
- Charlie sees: Only settlements where he is debtor or creditor
- Admin sees: All settlements in the Nex
```

### **GROUP Nex Type**

**Group-focused settlement management**

- **Settlement Visibility**: All users see all settlements in the group
- **Settlement Execution**: All users can settle any debts in the group
- **Group Behavior**: Everyone has group-level access
- **Use Case**: Shared expenses, group trips, collaborative spending

**Example:**

```
GROUP Nex with Alice, Bob, Charlie
- Alice sees: All settlements in the group
- Bob sees: All settlements in the group
- Charlie sees: All settlements in the group
- Everyone can settle any debts in the group
```

---

## 👀 **What Users See in Different Settlement Types**

### **DETAILED Settlement Type**

**Shows ALL individual debt transactions for each expense**

**Example Scenario:**

- Alice paid $100 for dinner
- Split equally among Alice, Bob, Charlie (3 people)
- Each person owes $33.33

**What Users See:**

```
Debt 1: Bob owes Alice $33.33 (Dinner expense)
Debt 2: Charlie owes Alice $33.33 (Dinner expense)
Debt 3: Alice owes Alice $33.33 (Dinner expense)
```

**Benefits:**

- ✅ Complete transparency
- ✅ Shows exact expense details
- ✅ Easy to track individual transactions
- ✅ Good for detailed record keeping

**Drawbacks:**

- ❌ More transactions to manage
- ❌ Can be overwhelming with many expenses
- ❌ Self-debts (Alice owes Alice) are confusing

### **SIMPLIFIED Settlement Type**

**Shows NET BALANCE transactions (minimum transactions to settle all debts)**

**Same Scenario:**

- Alice paid $100 for dinner
- Split equally among Alice, Bob, Charlie (3 people)
- Net balances: Alice = +$33.33, Bob = -$33.33, Charlie = -$33.33

**What Users See:**

```
Settlement 1: Bob pays Alice $33.33
Settlement 2: Charlie pays Alice $33.33
(Alice's self-debt is automatically netted out)
```

**Benefits:**

- ✅ Fewer transactions to manage
- ✅ Clear net balances
- ✅ Easier to understand who owes what
- ✅ No confusing self-debts

**Drawbacks:**

- ❌ Less detailed expense tracking
- ❌ Harder to see which expenses created which debts
- ❌ May lose some audit trail

---

## 🔄 **Settlement Type Changes**

### **Changing from SIMPLIFIED → DETAILED**

**What Happens:**

1. ✅ **Existing settlements remain unchanged**
2. ✅ **New expenses create detailed debt transactions**
3. ✅ **Users see individual debts for new expenses**
4. ✅ **No automatic recalculation of existing debts**

**Example:**

```
Before Change (SIMPLIFIED):
- Settlement: Bob pays Alice $50 (net balance)

After Change (DETAILED):
- Existing: Bob pays Alice $50 (unchanged)
- New Expense: Dinner $30 split 3 ways
  - Debt 1: Bob owes Alice $10 (Dinner)
  - Debt 2: Charlie owes Alice $10 (Dinner)
  - Debt 3: Alice owes Alice $10 (Dinner)
```

### **Changing from DETAILED → SIMPLIFIED**

**What Happens:**

1. ✅ **Existing settlements remain unchanged**
2. ✅ **New expenses create simplified net balance transactions**
3. ✅ **Users see net balance settlements for new expenses**
4. ✅ **No automatic recalculation of existing debts**

**Example:**

```
Before Change (DETAILED):
- Debt 1: Bob owes Alice $20 (Lunch)
- Debt 2: Charlie owes Alice $15 (Coffee)

After Change (SIMPLIFIED):
- Existing debts remain unchanged
- New Expense: Dinner $45 split 3 ways
  - Settlement: Bob pays Alice $15 (net balance)
  - Settlement: Charlie pays Alice $15 (net balance)
```

---

## 🎯 **Settlement Execution Examples**

### **PERSONAL Nex - Admin Settlement (Full Nex)**

```http
POST /api/v1/nex/{nexId}/settlements/execute
{
  "settlementType": "SIMPLIFIED",
  "settleAll": true,
  "paymentMethod": "CASH",
  "notes": "Monthly settlement"
}
```

**Result:** All debts in the PERSONAL Nex are settled using simplified net balance calculations.

### **PERSONAL Nex - User Settlement (Personal Settlements Only)**

```http
POST /api/v1/nex/{nexId}/settlements/execute
{
  "settlementType": "DETAILED",
  "settlementIds": ["settlement-1", "settlement-2"],
  "paymentMethod": "BANK_TRANSFER",
  "notes": "Settling my dinner settlements"
}
```

**Result:** Only the specified settlement transactions are executed (if user is involved in them).

### **PERSONAL Nex - User Settlement (Invalid - Not Involved)**

```http
POST /api/v1/nex/{nexId}/settlements/execute
{
  "settlementType": "DETAILED",
  "settlementIds": ["settlement-3"], // User not involved in this settlement
  "paymentMethod": "CASH"
}
```

**Result:** `AUTHZ_SETTLEMENT_DENIED` error - User cannot settle settlements they're not involved in.

### **GROUP Nex - Any User Settlement (Full Nex)**

```http
POST /api/v1/nex/{nexId}/settlements/execute
{
  "settlementType": "SIMPLIFIED",
  "settleAll": true,
  "paymentMethod": "CASH",
  "notes": "Group settlement"
}
```

**Result:** All debts in the GROUP Nex are settled - any user can do this.

### **GROUP Nex - User Settlement (Any Settlements)**

```http
POST /api/v1/nex/{nexId}/settlements/execute
{
  "settlementType": "DETAILED",
  "settlementIds": ["settlement-1", "settlement-2", "settlement-3"],
  "paymentMethod": "BANK_TRANSFER",
  "notes": "Settling group settlements"
}
```

**Result:** Any settlement transactions can be executed by any user in a GROUP Nex.

---

## 🧹 **Clean API Separation**

### **Debt Management vs Settlement Execution**

The API has been cleaned up to provide clear separation of concerns:

#### **DebtController - Debt Management Only**

```http
# Debt CRUD Operations
POST   /api/v1/debts                    # Create debt
GET    /api/v1/debts/{debtId}           # Get debt by ID
PUT    /api/v1/debts/{debtId}           # Update debt
DELETE /api/v1/debts/{debtId}           # Delete debt

# Debt Queries
GET    /api/v1/debts                    # Get all debts (paginated)
GET    /api/v1/debts/user/{userId}      # Get debts by user
GET    /api/v1/debts/nex/{nexId}        # Get debts by nex

# Balance Queries
GET    /api/v1/debts/user/{userId}/balance    # Get user balance
GET    /api/v1/debts/nex/{nexId}/balances     # Get nex balances
```

#### **SettlementController - Settlement Execution Only**

```http
# Settlement Operations
GET    /api/v1/nex/{nexId}/settlements/available    # Get available settlements
POST   /api/v1/nex/{nexId}/settlements/execute      # Execute settlements
GET    /api/v1/nex/{nexId}/settlements/history      # Get settlement history
GET    /api/v1/nex/{nexId}/settlements/analytics    # Get settlement analytics
GET    /api/v1/nex/{nexId}/settlements/summary      # Get settlement summary
```

### **Frontend Flow - Always Use Settlements**

**✅ Correct Flow:**

```javascript
// 1. Get available settlements
GET /api/v1/nex/{nexId}/settlements/available
→ Returns: List of SettlementTransaction objects with IDs

// 2. Execute settlements
POST /api/v1/nex/{nexId}/settlements/execute
→ Body: { settlementIds: ["settlement-1", "settlement-2"], ... }
→ Backend handles: Converting settlement transactions to individual debt settlements
```

**❌ Old Confusing Flow (Removed):**

```javascript
// This is no longer available - was confusing
POST / api / v1 / debts / { debtId } / settle; // ❌ Removed
```

### **Benefits of Clean Separation**

1. **Clear Responsibilities**: Debt management vs Settlement execution
2. **No Confusion**: Frontend always works with settlements
3. **Consistent API**: Single path for settlement operations
4. **Better UX**: Frontend doesn't need to know about individual debts
5. **Maintainable**: Clear separation of concerns

---

## 📊 **Available Settlements Endpoint**

### **Get Available Settlements**

```http
GET /api/v1/nex/{nexId}/settlements/available?settlementType=SIMPLIFIED
```

### **PERSONAL Nex - User Response (Filtered)**

**Response for SIMPLIFIED (User Bob):**

```json
{
  "availableSettlements": [
    {
      "id": "settlement-1",
      "fromUserId": "bob",
      "toUserId": "alice",
      "amount": 20.0,
      "settlementType": "SIMPLIFIED",
      "status": "PENDING"
    }
  ],
  "settlementType": "SIMPLIFIED",
  "totalAvailable": 1,
  "totalAmount": 20.0
}
```

**Response for DETAILED (User Bob):**

```json
{
  "availableSettlements": [
    {
      "id": "debt-1",
      "fromUserId": "bob",
      "toUserId": "alice",
      "amount": 20.0,
      "settlementType": "DETAILED",
      "status": "PENDING",
      "expenseId": "expense-1",
      "expenseTitle": "Dinner"
    }
  ],
  "settlementType": "DETAILED",
  "totalAvailable": 1,
  "totalAmount": 20.0
}
```

### **GROUP Nex - User Response (All Settlements)**

**Response for SIMPLIFIED (Any User):**

```json
{
  "availableSettlements": [
    {
      "id": "settlement-1",
      "fromUserId": "bob",
      "toUserId": "alice",
      "amount": 20.0,
      "settlementType": "SIMPLIFIED",
      "status": "PENDING"
    },
    {
      "id": "settlement-2",
      "fromUserId": "charlie",
      "toUserId": "alice",
      "amount": 15.0,
      "settlementType": "SIMPLIFIED",
      "status": "PENDING"
    }
  ],
  "settlementType": "SIMPLIFIED",
  "totalAvailable": 2,
  "totalAmount": 35.0
}
```

**Response for DETAILED (Any User):**

```json
{
  "availableSettlements": [
    {
      "id": "debt-1",
      "fromUserId": "bob",
      "toUserId": "alice",
      "amount": 20.0,
      "settlementType": "DETAILED",
      "status": "PENDING",
      "expenseId": "expense-1",
      "expenseTitle": "Dinner"
    },
    {
      "id": "debt-2",
      "fromUserId": "charlie",
      "toUserId": "alice",
      "amount": 15.0,
      "settlementType": "DETAILED",
      "status": "PENDING",
      "expenseId": "expense-2",
      "expenseTitle": "Coffee"
    }
  ],
  "settlementType": "DETAILED",
  "totalAvailable": 2,
  "totalAmount": 35.0
}
```

---

## 🚨 **Error Scenarios**

### **Authorization Errors**

```json
{
  "success": false,
  "message": "Only admins can settle all debts in a Nex",
  "errorCode": "AUTHZ_SETTLEMENT_DENIED"
}
```

### **User Not Involved in Debt**

```json
{
  "success": false,
  "message": "User alice cannot settle debt debt-123 - not involved in this debt",
  "errorCode": "AUTHZ_SETTLEMENT_DENIED"
}
```

### **Nex Not Found**

```json
{
  "success": false,
  "message": "Nex not found",
  "errorCode": "NEX_NOT_FOUND"
}
```

---

## 💡 **Best Practices**

### **For Admins:**

1. **Use SIMPLIFIED for regular settlements** - Easier for users to understand
2. **Use DETAILED for audit purposes** - When detailed tracking is needed
3. **Communicate settlement type changes** - Let users know what to expect
4. **Settle regularly** - Don't let debts accumulate

### **For Users:**

1. **Check available settlements** - See what you can settle
2. **Settle your debts promptly** - Keep your balance clean
3. **Use appropriate payment methods** - Match your actual payment method
4. **Add notes for clarity** - Help others understand the settlement

### **For Developers:**

1. **Always validate user permissions** - Check admin status and debt involvement
2. **Handle settlement type changes gracefully** - Don't break existing data
3. **Provide clear error messages** - Help users understand what went wrong
4. **Log settlement activities** - For audit and debugging purposes

---

## 🔧 **Technical Implementation Notes**

### **Settlement Algorithms**

- **SIMPLIFIED**: Uses net balance calculation with greedy algorithm to minimize transactions
- **DETAILED**: Groups debts by creditor-debtor pairs, shows all individual transactions

### **Database Views**

- Uses `settlement_history_view` for optimized settlement queries
- Provides comprehensive settlement tracking with user details

### **Authorization Flow**

1. Validate Nex exists and user is member
2. Check user role (admin vs member)
3. Validate debt involvement for specific settlements
4. Execute settlements based on permissions

### **Settlement Type Handling**

- Settlement type is stored in `Nex.settlementType` field
- Changes don't affect existing settlements
- New expenses use the current settlement type
- No automatic recalculation of existing debts
