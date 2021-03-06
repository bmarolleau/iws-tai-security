package com.ibm.sample;

import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ibm.websphere.security.WebTrustAssociationException;
import com.ibm.websphere.security.WebTrustAssociationFailedException;
import com.ibm.wsspi.security.tai.TAIResult;
import com.ibm.websphere.security.jwt.Claims;
import com.ibm.websphere.security.jwt.InvalidConsumerException;
import com.ibm.websphere.security.jwt.InvalidTokenException;
import com.ibm.websphere.security.jwt.JwtConsumer;
import com.ibm.websphere.security.jwt.JwtToken;
import com.ibm.wsspi.security.tai.TrustAssociationInterceptor;

public class SimpleTAI implements TrustAssociationInterceptor 
{
   public SimpleTAI()
   {
      super();
   }

   public boolean isTargetInterceptor(HttpServletRequest req)
   throws WebTrustAssociationException {
//Add logic to determine whether to intercept this request, i.e. this TAI is going to authenticate the user

boolean match = false;
//Verify that the Authorization header contains a JWT
if (req.getHeader("Authorization")!=null) {
   System.out.println("[JWTTAI] HTTP header Authorization = " + req.getHeader("Authorization"));
   match = req.getHeader("Authorization").matches("Bearer ([a-zA-Z0-9]|-|_)+\\.([a-zA-Z0-9]|-|_)+\\.([a-zA-Z0-9]|-|_|=)+");
   System.out.println("[JWTTAI] HTTP header Authorization match = " + match);
}
//If request is using HTTPS and does contain a JWT then intercept
if (req.isSecure() && match){
   return true;
}
return false;
}

public TAIResult negotiateValidateandEstablishTrust(HttpServletRequest req,
HttpServletResponse resp) throws WebTrustAssociationFailedException {
//Add logic to authenticate an user and return a TAI result.

// Get the Authorization header from the HTTP request and get the second part (Bearer <token>)
String tai_user = "";
String jwtTokenString = req.getHeader("Authorization").split(" ")[1];

// Decode  the JWT using the JwtConsumer to obtain the claims
JwtConsumer jwtConsumer;
try {
// Use the myJWTConsumer configuration (cf. server.xml)
jwtConsumer = JwtConsumer.create("myJWTConsumer");

JwtToken jwtTokenConsumer = jwtConsumer.createJwt(jwtTokenString);
Claims jwtClaims = jwtTokenConsumer.getClaims();
// Uncomment to print the JWT claims in messages.log
/* 
System.out.println("[JWTTAI] Decoded JWT token is as follows: ");
for(Entry<String, Object> e : jwtClaims.entrySet()) {
     System.out.println("[JWTTAI] " + e.getKey()+" :: "+e.getValue());
} */

tai_user = jwtClaims.getSubject();
System.out.println("[JWTTAI] TAI USER = " + tai_user);
} catch (InvalidConsumerException e) {
//e.printStackTrace();
System.err.println("[JWTTAI] " + e.getMessage());
return TAIResult.create(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
} catch (InvalidTokenException e) {
//e.printStackTrace();
System.err.println("[JWTTAI] " + e.getMessage());
return TAIResult.create(HttpServletResponse.SC_UNAUTHORIZED);
}
 
return TAIResult.create(HttpServletResponse.SC_OK, tai_user);
}

   public int initialize(Properties arg0) throws WebTrustAssociationFailedException 
   {
        return 0;
   }

   public String getVersion() 
   {
        return "1.0";
   }

   public String getType() 
   {
        return this.getClass().getName();
   }

   public void cleanup() { }
}

