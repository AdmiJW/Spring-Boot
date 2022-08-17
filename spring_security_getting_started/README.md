# Spring Security 🔒


[**!! AWESOME REFERENCE HERE**](https://www.marcobehler.com/guides/spring-security). This note is based off the reference


---

## 1. Spring Security Overview

* Spring Security is just a bunch of servlet filters that help you to add authentication and authorization to your web application. It integrates well with frameworks like Spring Web MVC or Spring Boot, and standards like OAuth2 or SAML. It even auto-generates login/logout pages and protects against common exploits like CSRF (Cross Site Request Forgery).

* **Authentication** - Verifying the user is indeed who he claims to be, usually done with username + password checks.

* **Authorization** - Permissions - Verifying the user has the right to access the resource. Eg: user groups like Admin and Guest

* **Servlet Filters** - Any Spring web application is basically just **one** servlet: `DispatcherServlet` that redirects incoming HTTP requests to your `@Controllers` or `@RestControllers`. 

* Sadly, there is no security features inside the `DispatcherServlet` itself, and we do not want to implement securities inside our own `@Controllers`! (It would be disasterous). Optimally, the authentication and authorization should be done before the request even hit our `@Controllers`!

* Therefore, we can add **filters** in front of servlets. Think about writing a `SecurityFilter` and configure it in the Tomcat (servlet container/application server) to filter every incoming HTTP request before it hits our servlet.

    ![Basic Concept of SecurityFilter](https://www.marcobehler.com/images/servletfilter-1a.png)

* A very basic SecurityFilter that filters HTTP requests, may conceptually look like the following: *(Don't try to understand the exact method, just look at the name and grasp the concept of what it does)*

    ```java
    import javax.servlet.*;
    import javax.servlet.http.HttpFilter;
    import javax.servlet.http.HttpServletRequest;
    import javax.servlet.http.HttpServletResponse;
    import java.io.IOException;

    public class SecurityServletFilter extends HttpFilter {

        @Override
        protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

            // Extract username/password from request, like from Basic Auth HTTP Header, request body, or cookie, etc.
            UsernamePasswordToken token = extractUsernameAndPasswordFrom(request); 

            // No username password provided, or incorrect password/username after checking with database. 
            if (notAuthenticated(token)) {  
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // HTTP 401.
                return;
            }

            // Checking the authorization of authenticated user
            if (notAuthorized(token, request)) { 
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            // Request survived. Authenticated and Authorized. Redirect to our DispatcherServlet (@Controllers)
            chain.doFilter(request, response); 
        }

        private UsernamePasswordToken extractUsernameAndPasswordFrom(HttpServletRequest request) {
            // Either try and read in a Basic Auth HTTP Header, which comes in the form of user:password
            // Or try and find form login request parameters or POST bodies, i.e. "username=me" & "password="myPass"
            return checkVariousLoginOptions(request);
        }


        private boolean notAuthenticated(UsernamePasswordToken token) {
            // compare the token with what you have in your database...or in-memory...or in LDAP...
            return false;
        }

        private boolean notAuthorized(UsernamePasswordToken token, HttpServletRequest request) {
        // check if currently authenticated user has the permission/role to access this request's /URI
        // e.g. /admin needs a ROLE_ADMIN , /callcenter needs ROLE_CALLCENTER, etc.
        return false;
        }
    }
    ```

* The above `SecurityServletFilter` is just a conceptually working model. If we were to put reality implementations, it would grew into monsterous, gigantic filter with tons of codes. 

* Therefore, we would use modular design and split into multiple filters, then chain them together. Eg: *(Incoming Request) -> (Login Method Filter) -> (Authentication Filter) -> (Authorization Filter) -> (Servlet)*. With this approach, we don't even have to fondle with our business logic implementation (the `@Controllers`)


---

## 2. The FilterChain & Security Configuration DSL

* The default Spring Security's FilterChain comes with around 15 different filters, which every HTTPRequest has to go through before hitting your `@Controllers`!

    ![Spring Security's Default FilterChain](https://www.marcobehler.com/images/filterchain-1a.png)

    * `BasicAuthenticationFllter` - Tries to find Basic Auth HTTP Header on the request, and if found, tries to authenticate the user

    * `UsernamePasswordAuthenticationFilter` - Tries to find username/password request parameter/POST body, and authenticate the user if found.

    * `DefaultLoginPageGeneratingFilter` - Generates a login page for you, if you dont explicitly disable it.

    * `DefaultLogoutPageGeneratingFilter` - Generates a logout page for you, if you don't explicitly disable it.

    * `FilterSecurityInterceptor` - Does your authorization.

* These filters, for a large part, is Spring Security. You as the developer are responsible to set up configurations: Which URL to protect, which to ignore, and what database table are used for authentication.

---

* To configure Spring Security, we need a class that:

    1. Annotated with `@EnableWebSecurity`

    1. Extends WebSecurityConfigurer, which basically offers you a configuration DSL/methods. These methods allow us to specify what URIs to protect, or what exploit protections to enable/disable.

* Something like this:

    ```java
    @Configuration
    @EnableWebSecurity
    public class WebSecurityConfig extends WebSecurityConfigurerAdapter { // (1)

    @Override
    protected void configure(HttpSecurity http) throws Exception {  
        http
            .authorizeRequests()
            // No need authentication for '/' and '/home'
            .antMatchers("/", "/home").permitAll() 
            // Other requests require authentication
            .anyRequest().authenticated() // (4)
            .and()
            // Allow form login with custom loginPage (/login)
            // and of course, no need authentication to access login form!
        .formLogin() 
            .loginPage("/login") 
            .permitAll()
            .and()
            // The same goes for logout page
        .logout()
            .permitAll()
            .and()
            // Allowing Basic Auth - Authenticate from HTTP Basic Auth Header
        .httpBasic(); 
    }
    }
    ```

* This `configure` method, is where we specify:

    1. What URLs to protect, and which ones are public
    
    1. What authentication methods are allowed: `formLogin()` and `httpBasic()`, along with its configurations

    1. and many others...

* If you didn't override the `configure()` method, it comes with a default implementation:

    ```java
    public abstract class WebSecurityConfigurerAdapter implements
            WebSecurityConfigurer<WebSecurity> {

        protected void configure(HttpSecurity http) throws Exception {
                http
                    .authorizeRequests()
                        .anyRequest().authenticated() 
                        .and()
                    .formLogin().and()  
                    .httpBasic(); 
            }
    }
    ```

---

## 3. Authentication

* There are usually 3 cases for authentication:

    1. You have full access to the (hashed) password of the user. Eg: saved in database

    1. You have no access to the (hashed) password of the user. Eg: 3rd party identity management product with REST services for authentication (Like Atlassian Crowd)

    1. OAuth2, aka login with Google/Twitter etc, with JWT (JSON Web Tokens). Explained more detail in another section below

* Based on different scenario, different `@Beans` has to be specified to get Spring Security working.

    ---

### 3.1 `UserDetailsService` - You have access to user's password

* For this case, you need to define two beans: `UserDetailsService` (to retrieve user details) and `PasswordEncoder`.

* A `UserDetailsService` is simply like:

    ```java
    @Bean
    public UserDetailsService userDetailsService() {
        return new MyDatabaseUserDetailsService(); 
    }
    ```

    where `MyDatabaseUserDetailsService` implements `UserDetailsService`, which has one method to return `UserDetails` object:

    ```java
    public class MyDatabaseUserDetailsService implements UserDetailsService {

        UserDetails loadUserByUsername(String username) throws UsernameNotFoundException { // (1)
            // 1. Load the user from the users table by username. If not found, throw UsernameNotFoundException.
            // 2. Convert/wrap the user to a UserDetails object and return it.
            return someUserDetails;
        }
    }

    // UserDetails must have methods to get the username, and **hashed** password
    public interface UserDetails extends Serializable { // (2)

        String getUsername();

        String getPassword();

        // more methods available (if you need it):
        // isAccountNonExpired,isAccountNonLocked,
        // isCredentialsNonExpired,isEnabled
    }
    ```

* We can implement the `UserDetailsService` and `UserDetails` interfaces ourselves, or use off-the-shelf implementations by the Spring Security, and configure/extend/override it:

    1. `JdbcUserDetailsManager` - JDBC(database)-based `UserDetailsService`. Configure it to match user table/column structure.

    1. `InMemoryUserDetailsManager` - `UserDetailsService` that keep all user details in memory, great for testing environment

    1. `org.springframework.security.core.userdetail.User` - sensible, default `UserDetails` implementation.

* Therefore, this is the basic flow:

    1. Extract username/password combination from HTTP Basic Auth header in a filter

    1. Call your `MyDatabaseUserDetailsService` to load the user from the database, wrapped as UserDetails object, which exposes the user's hashed password

    1. Take the extracted password, hash it *automatically* and compare with the hashed password of your `UserDetails` object.

* As you can see from step 3, we need a `PasswordEncoder` `@Bean` to specify our preferred password hashing algorithm (such as BCrypt, the default).

* Therefore, we might specify this `@Bean` in our `SecurityConfig`:

    ```java
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    ```

* If we have multiple password hashing algorithms (like some legacy users whose passwords are stored with MD5, and new users with password hashed with Bcrypt/SHA-256), we can specify them as follows:

    ```java
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
    ```

* This delegating encoder look at the `UserDetail`'s hashed password, which has a `{prefix}` specifying the hashing algorithm used! like `{bcrypt}$2y$12$6t86Rpr3llMANhCUt26oUen2WhvXr/A89Xo9zJion8W7gWgZ/zA0C` or `{sha256}5ffa39f5757a0dad5dfada519d02c6b71b61ab1df51b4ed1f3beed6abe0ff5f6`

    ---

### 3.2 `AuthenticationProvider` - You have no access to user's password

* Imagine using third party identity management products like Atlassian Crowd. The passwords are not stored in your database, but on Atlassian Crowd's server!

    * We do not have password to compare with anmore, and we cannot ask Atlassian Crowd to send us the password

    * However, we have a REST API to login against, and send the username & password for authentication

* In such case, we have to instead implement an `AuthenticationProvider` `@Bean`

    ```java
    @Bean
    public AuthenticationProvider authenticationProvider() {
        return new AtlassianCrowdAuthenticationProvider();
    }
    ```

    and the `AuthenticationProvider` shall consists primarily of one method, and a naive implementation could look like this:

    ```java
    public class AtlassianCrowdAuthenticationProvider implements AuthenticationProvider {

            Authentication authenticate(Authentication authentication) 
                    throws AuthenticationException {
                // Compared to UserDetails load() method, we have full access to username and password of the authentication attempt
                String username = authentication.getPrincipal().toString(); 
                String password = authentication.getCredentials().toString();

                // Do whatever you need to authenticate
                User user = callAtlassianCrowdRestService(username, password); 
                if (user == null) {                      
                    // Throw AuthenticationException on failed login
                    throw new AuthenticationException("could not login");
                }
                // Success - Return fully initialized UsernamePasswordAuthenticationToken
                return new UserNamePasswordAuthenticationToken(user.getUsername(), user.getPassword(), user.getAuthorities()); 
            }
            // other method not explained here
    }
    ```

* So the basic flow goes like this:

    1. Extract username/password combination from HTTP Basic Auth header in a filter

    1. Call your `AuthenticationProvider` to authenticate remotely. Perform authentication logic yourself. No password hashing or similar is required, all of that is done remotely by third party identity management product.

---

## 4. Authorization

* A more complex application would not work with just authentication. It will have different views for different users, and authorization is required. Let's look at Spring Security terminologies first:

  * **Authority** - Put simply, a simple string like `ADMIN`, `ROLE_ADMIN`, `user` etc.

  * **Role** is an authority with a `ROLE_` prefix - `ROLE_ADMIN`, `ROLE_USER` etc

* Using Spring Security, we would need a Java class to represent our authority String, a popular one being `SimpleGrantedAuthority`.

    ```java
    public final class SimpleGrantedAuthority implements GrantedAuthority {

        private final String role;

        @Override
        public String getAuthority() {
            return role;
        }
    }
    ```

* In our database, we would store the authority as a string *(or we could have multiple, comma-separated values)* column in the Users table *(or we could have separate table for storing authorities)*. 

* So now, we would have to also read and provide the authorities in `UserDetailsService`:

    ```java
    public class MyDatabaseUserDetailsService implements UserDetailsService {

        UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
            User user = userDao.findByUsername(username);
            List<SimpleGrantedAuthority> grantedAuthorities = user.getAuthorities().map(authority -> new SimpleGrantedAuthority(authority)).collect(Collectors.toList());
            return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), grantedAuthorities); 
        }

    }
    ```

    Similar concept if you are using a third party identity management product. You would have to retrieve the authorities yourself and provide them in the `AuthenticationProvider`.

* With authorities set up properly, we can now protect URLs from unauthorized authorities inside our `configure()` method in `WebSecurityConfigurerAdapter`:

    ```java
    @Configuration
    @EnableWebSecurity
    public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http
                .authorizeRequests()
                    .antMatchers("/admin").hasAuthority("ROLE_ADMIN") // (1)
                    .antMatchers("/callcenter").hasAnyAuthority("ROLE_ADMIN", "ROLE_CALLCENTER") // (2)
                    .anyRequest().authenticated() // (3)
                    .and()
                .formLogin()
                .and()
                .httpBasic();
        }
    }
    ```

* Finally, you have the option to use Spring Expression Language (SpEL) with `access()` method to setup the authorization:

    ```java
    http
        .authorizeRequests()
            .antMatchers("/admin").access("hasRole('admin') and hasIpAddress('192.168.1.0/24') and @myCustomBean.checkAccess(authentication,request)") 
    ```

---

## 5. Exploit Protection

* Spring Security helps you to protect against variety of common attacks: timing attacks (hash the supplied password on login, even if user does not exist), cache control attacks, content sniffing, click jacking, cross-site scripting etc.

* As from the reference, this note will explain about Cross-Site-Request-Forgery (CSRF) protection: protects any incoming POST (or PUT/DELETE/PATCH) request with a valid CSRF token.

* Spring Security will generate a CSRF token per HTTP session, stores it there and inject into HTML forms (maybe with the help of templating technology like Thymeleaf/FreeMarker). Once the form is submitted once, **the token is invalidated and the form cannot be submitted again**.

* Thymeleaf templating engine has good integration with Spring Security - it can have the CSRF token automatically injected into the form:

    ```html
    <form action="/transfer" method="post">  
        <input type="text" name="amount"/>
        <input type="text" name="routingNumber"/>
        <input type="text" name="account"/>
        <input type="submit" value="Transfer"/>
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
    </form>

    <!-- OR -->

    <form th:action="/transfer" method="post">  
        <input type="text" name="amount"/>
        <input type="text" name="routingNumber"/>
        <input type="text" name="account"/>
        <input type="submit" value="Transfer"/>
    </form>
    ```

* For other templating libraries, we can inject the CSRF token into the `@Controller` methods, and add it manually to the model to render it in a view, or access it directly as `HttpServletRequest` request attribute:

    ```java
    @Controller
    public class MyController {
        @GetMaping("/login")
        public String login(Model model, CsrfToken token) {
            // the token will be injected automatically
            return "/templates/login";
        }
    }
    ```

* For Javascript apps like React or Angular, the CSRF token has to take the shape of cookies. Refer to this article to see how it's done: [**ARTICLE**](https://developer.okta.com/blog/2022/06/17/simple-crud-react-and-spring-boot)

    1. Configure Spring Security to use `CookieCsrfTokenRepository`, which will put the `CSRFToken` into the browser's cookie as `XSRF-TOKEN`

    1. Make your javascript app to take the `XSRF-TOKEN` cookie value and send it as `X-XSRF-TOKEN` header in every POST(PUT/DELETE/PATCH) request.

* To disable CSRF, we can disable it completely:

    ```java
    @EnableWebSecurity
    @Configuration
    public class WebSecurityConfig extends
    WebSecurityConfigurerAdapter {

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http
                .csrf().disable();
        }
    }
    ```

---

## 6. OAuth2

[**!! Another Reference From Same Author**](https://www.marcobehler.com/guides/spring-security-oauth2#)


---

## 7. Spring Integrations

* So far, we had only setup security on the *web tier* of the application - using `antMatcher` or `regexMatchers` with `WebSecurityConfigurerAdapter`'s DSL. We might also want to "defense in depth" - Other than protecting from URL, we might want to protect our business logics - `@Controllers`, `@Components`, `@Services` or `@Repositories` (the Spring beans)

* This approach is called **method security**, implemented by putting annotations on public method of our Spring beans. That being said, we need to enable method security first from our `ApplicationContextConfiguration`:

    ```java
    @Configuration
    @EnableGlobalMethodSecurity(
        prePostEnabled = true,  // Allow @PreAuthorize, @PostAuthorize
        securedEnabled = true,  // Allow @Secured
        jsr250Enabled = true    // Allow @RolesAllowed
    ) 
    public class YourSecurityConfig extends WebSecurityConfigurerAdapter{
    }
    ```

* `@Secured` and `@RolesAllowed` are basically the same, just that they are from different packages. `@PreAuthorize` and `@PostAuthorize` are more powerful version of `@Secured` and `@RolesAllowed`, as they can also contain valid SpEL expression.

* All of these annotations will raise `AccessDeniedException` if you try and access a protected method with insufficient authority.

    ```java
    @Service
    public class SomeService {

        @Secured("ROLE_ADMIN") // Allow ADMIN authority only
        public BankAccountInfo get(...) {

        }

        @PreAuthorize("isAnonymous()") // This is SpEL expression. As said earlier, @PreAuthorize is more powerful
        public void trackVisit(Long id);

        }
    }
    ```