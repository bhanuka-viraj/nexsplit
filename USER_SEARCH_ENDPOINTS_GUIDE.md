# User Search Endpoints Guide

## 🔍 **Overview**

The user search functionality provides comprehensive search capabilities for finding users to invite to Nex groups. The system includes two search endpoints optimized for different use cases.

## 📋 **Available Endpoints**

### **1. General User Search (Paginated)**

```http
GET /api/v1/users/search?q={searchTerm}&page={page}&size={size}&sort={sort}
```

**Purpose:** Full-featured user search with pagination support.

**Parameters:**

- `q` (required): Search term (minimum 2 characters)
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 20, max: 100)
- `sort` (optional): Sort field (default: "firstName")

**Example:**

```http
GET /api/v1/users/search?q=john&page=0&size=10&sort=firstName
```

**Response:**

```json
{
  "success": true,
  "message": "Users found successfully",
  "data": {
    "data": [
      {
        "id": "user-123",
        "email": "john.doe@example.com",
        "username": "johndoe",
        "firstName": "John",
        "lastName": "Doe",
        "fullName": "John Doe",
        "contactNumber": "+1234567890",
        "profilePictureUrl": null,
        "isEmailVerified": true,
        "isGoogleAuth": false,
        "status": "ACTIVE"
      }
    ],
    "pagination": {
      "page": 0,
      "size": 10,
      "totalElements": 1,
      "totalPages": 1,
      "hasNext": false,
      "hasPrevious": false,
      "nextPageUrl": null,
      "previousPageUrl": null
    }
  },
  "timestamp": "2025-01-20T10:30:00Z"
}
```

### **2. Email-Only Search**

```http
GET /api/v1/users/search/email?email={emailAddress}
```

**Purpose:** Search users by email address only.

**Parameters:**

- `email` (required): Email address to search for

**Example:**

```http
GET /api/v1/users/search/email?email=alice.smith@example.com
```

**Response:**

```json
{
  "success": true,
  "message": "Users found successfully",
  "data": [
    {
      "id": "user-456",
      "email": "alice.smith@example.com",
      "username": "alicesmith",
      "firstName": "Alice",
      "lastName": "Smith",
      "fullName": "Alice Smith",
      "contactNumber": "+1987654321",
      "profilePictureUrl": null,
      "isEmailVerified": true,
      "isGoogleAuth": true,
      "status": "ACTIVE"
    }
  ],
  "timestamp": "2025-01-20T10:30:00Z"
}
```

## 🔍 **Search Capabilities**

### **Search Fields**

The general search (`/search`) searches across:

1. **Email Address** - Full or partial email matching
2. **Username** - Full or partial username matching
3. **First Name** - Full or partial first name matching
4. **Last Name** - Full or partial last name matching

### **Search Behavior**

- **Case Insensitive**: All searches are case-insensitive
- **Partial Matching**: Uses `LIKE` with wildcards for partial matching
- **Active Users Only**: Only returns active, non-deleted users
- **Ordered Results**: Results are ordered by firstName, lastName, username

### **Search Examples**

| Search Term    | Matches                                                            |
| -------------- | ------------------------------------------------------------------ |
| `john`         | Users with "john" in email, username, first name, or last name     |
| `john.doe`     | Users with "john.doe" in email, username, first name, or last name |
| `@example.com` | Users with "@example.com" in their email                           |
| `smith`        | Users with "smith" in email, username, first name, or last name    |

## 🔐 **Security & Authorization**

### **Authentication Required**

All search endpoints require valid JWT authentication:

```http
Authorization: Bearer {jwt_token}
```

### **User Privacy**

- **No Sensitive Data**: Search results don't include sensitive information
- **Active Users Only**: Only active, non-deleted users are returned
- **Audit Logging**: All search activities are logged for audit purposes

### **Rate Limiting**

- **Search Query Validation**: Minimum 2 characters required
- **Result Limits**: Use pagination to limit results
- **Pagination**: General search supports pagination to prevent large result sets

## 📊 **Use Cases**

### **1. Nex Group Invitations**

```javascript
// Frontend: Search for users to invite to a Nex group
const searchUsers = async (searchTerm, page = 0, size = 20) => {
  const response = await fetch(
    `/api/v1/users/search?q=${searchTerm}&page=${page}&size=${size}`,
    {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    }
  );
  return response.json();
};
```

### **2. User Autocomplete**

```javascript
// Frontend: Autocomplete for user selection
const handleUserInput = async (input) => {
  if (input.length >= 2) {
    const results = await searchUsers(input, 0, 20); // Limit to 20 results
    setUserSuggestions(results.data.data); // Access data.data for the array
  }
};
```

### **3. Email-Based User Lookup**

```javascript
// Frontend: Find specific user by email
const findUserByEmail = async (email) => {
  const response = await fetch(`/api/v1/users/search/email?email=${email}`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
  return response.json();
};
```

### **4. Paginated User Management**

```javascript
// Frontend: Paginated user list for admin interfaces
const getUsersPage = async (searchTerm, page, size) => {
  const response = await fetch(
    `/api/v1/users/search?q=${searchTerm}&page=${page}&size=${size}`,
    {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    }
  );
  return response.json();
};
```

## 🚨 **Error Handling**

### **Common Error Responses**

#### **401 Unauthorized**

```json
{
  "success": false,
  "message": "Unauthorized - Invalid or missing token",
  "errorCode": "UNAUTHORIZED"
}
```

#### **400 Bad Request - Empty Query**

```json
{
  "success": false,
  "message": "Search query cannot be empty",
  "errorCode": "BAD_REQUEST"
}
```

#### **400 Bad Request - Query Too Short**

```json
{
  "success": false,
  "message": "Search query must be at least 2 characters long",
  "errorCode": "BAD_REQUEST"
}
```

#### **400 Bad Request - Empty Email**

```json
{
  "success": false,
  "message": "Email parameter cannot be empty",
  "errorCode": "BAD_REQUEST"
}
```

## 📈 **Performance Considerations**

### **Database Optimization**

- **Indexed Fields**: Email, username, first name, and last name are indexed
- **Active User Filter**: Only queries active, non-deleted users
- **Result Limiting**: Use pagination to limit results
- **Pagination**: General search supports pagination

### **Caching Strategy**

- **No Caching**: Search results are not cached to ensure real-time data
- **Database Views**: Uses optimized database queries
- **Connection Pooling**: Efficient database connection management

## 🔧 **Technical Implementation**

### **Database Queries**

```sql
-- General search query
SELECT u FROM User u WHERE
  u.deletedAt IS NULL AND
  u.status = 'ACTIVE' AND
  (LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR
   LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR
   LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR
   LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')))
ORDER BY u.firstName, u.lastName, u.username
```

### **Response DTO Structure**

```java
public class UserSearchDto {
    private String id;
    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private String fullName;
    private String contactNumber;
    private String profilePictureUrl;
    private Boolean isEmailVerified;
    private Boolean isGoogleAuth;
    private String status;
}
```

## 🎯 **Best Practices**

### **For Frontend Developers**

1. **Use General Search for Autocomplete**: Use `/search` with small page size for real-time suggestions
2. **Use General Search for Pagination**: Use `/search` for comprehensive user lists
3. **Use Email Search for Specific Lookups**: Use `/search/email` when you have an exact email
4. **Implement Debouncing**: Add debouncing to prevent excessive API calls
5. **Handle Empty Results**: Gracefully handle cases with no search results

### **For Backend Developers**

1. **Monitor Search Performance**: Track search query performance
2. **Log Search Activities**: All searches are logged for audit purposes
3. **Validate Input**: Always validate search parameters
4. **Limit Results**: Implement appropriate result limits
5. **Use Indexes**: Ensure database indexes are optimized for search fields

## 📝 **Example Integration**

### **React Component Example**

```jsx
import React, { useState, useEffect } from "react";

const UserSearchComponent = () => {
  const [searchTerm, setSearchTerm] = useState("");
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(false);

  const searchUsers = async (term) => {
    if (term.length < 2) {
      setUsers([]);
      return;
    }

    setLoading(true);
    try {
      const response = await fetch(
        `/api/v1/users/search?q=${term}&page=0&size=20`,
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );
      const data = await response.json();
      setUsers(data.data.data); // Access data.data for the array
    } catch (error) {
      console.error("Search failed:", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    const timeoutId = setTimeout(() => {
      searchUsers(searchTerm);
    }, 300); // Debounce for 300ms

    return () => clearTimeout(timeoutId);
  }, [searchTerm]);

  return (
    <div>
      <input
        type="text"
        value={searchTerm}
        onChange={(e) => setSearchTerm(e.target.value)}
        placeholder="Search users..."
      />
      {loading && <div>Searching...</div>}
      <ul>
        {users.map((user) => (
          <li key={user.id}>
            {user.fullName} ({user.email})
          </li>
        ))}
      </ul>
    </div>
  );
};
```

This comprehensive user search functionality provides flexible, secure, and efficient user discovery capabilities for the NexSplit application! 🎉
