/*
* Copyright (C) 2005-2013 Alfresco Software Limited.
*
* This file is part of Alfresco
*
* Alfresco is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Alfresco is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
*/

package org.alfresco.web.scripts.servlet;

import javax.security.auth.x500.X500Principal;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.cert.X509Certificate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 *  The X509ServletFilterBase enforces X509 Authentication.
 *
 *  Optional Init Param:
 *  <b>cert-contains</b> : Ensure that the principal subject of the cert contains a specific string.
 *
 *  The X509ServletFilter will also ensure that the cert is present in the request, which will only happen if there
 *  is a successful SSL handshake which includes client authentication. This handshake is handled by the Application Server.
 *  A SSL handshake that does not include client Authentication will receive a 403 error response.
 *
 *  The checkInforce method must be implemented to determine if the X509 Authentication is turned on. This allows
 *  applications to turn on/off X509 Authentication based on parameters outside of the web.xml.
 *
 * */

public abstract class X509ServletFilterBase implements Filter
{

    protected boolean enforce;
    private String certContains;
    private static Log logger = LogFactory.getLog(X509ServletFilterBase.class);

    public void init(FilterConfig config) throws ServletException
    {
        try
        {
            /*
            *  Find out if we are enforcing.
            */

            if(logger.isDebugEnabled())
            {
                logger.debug("Initializing X509ServletFilter");
            }

            this.enforce = checkEnforce(config.getServletContext());

            if(logger.isDebugEnabled())
            {
                logger.debug("Enforcing X509 Authentication:"+this.enforce);
            }

            if (this.enforce)
            {
                /*
                * We are enforcing so get the cert-contains string.
                */

                this.certContains = config.getInitParameter("cert-contains");

                if(logger.isDebugEnabled())
                {
                    if(certContains == null)
                    {
                        logger.debug("Not enforcing cert-contains");
                    }
                    else
                    {
                        logger.debug("Enforcing cert-contains:" + this.certContains);
                    }
                }
            }
        }
        catch (Exception e)
        {
            throw new ServletException(e);
        }
    }

    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain) throws IOException,
            ServletException
    {

        HttpServletRequest httpRequest = (HttpServletRequest)request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        /*
        * Test if we are enforcing X509.
        */
        if(this.enforce)
        {
            if(logger.isDebugEnabled())
            {
                logger.debug("Enforcing X509 request");
            }

            X509Certificate[] certs = (X509Certificate[])httpRequest.getAttribute("javax.servlet.request.X509Certificate");
            if(validCert(certs))
            {

                if(logger.isDebugEnabled())
                {
                    logger.debug("Cert is valid");
                }

                /*
                * The cert is valid so forward the request.
                */

                chain.doFilter(request,response);
            }
            else
            {
                if(logger.isDebugEnabled())
                {
                    logger.debug("Cert is invalid");
                }

                /*
                * Invalid cert so send 403.
                */
                httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "X509 Authentication failure");
            }
        }
        else
        {
            /*
            * We are not enforcing X509 so forward the request
            */
            chain.doFilter(request,response);
        }
    }


    /**
     *
     * @param servletContext
     * @return true if enforcing X509 false if not enforcing X509
     * @throws IOException
     *
     *  The checkInforce method is called during the initialization of the Filter. Implement this method to decide if
     *  X509 security is being enforced.
     *
     **/

    protected abstract boolean checkEnforce(ServletContext servletContext) throws IOException;

    private boolean validCert(X509Certificate[] certs)
    {
        /*
        * If the cert is null then the it's not valid.
        */

        if(certs == null)
        {
            return false;
        }

        /*
        * Get the first certificate in the chain. The first certificate is the client certificate.
        */

        X509Certificate cert = certs[0];
        try
        {
            /*
            * check the certificate has not expired.
            */
            if(logger.isDebugEnabled())
            {
                logger.debug("Checking cert is valid");
            }
            cert.checkValidity();
        }
        catch (Exception e)
        {
            logger.error("Cert is invalid", e);
            return false;
        }

        X500Principal x500Principal = cert.getSubjectX500Principal();
        String name = x500Principal.getName();

        /*
        * Cert contains is an optional check
        */

        if(this.certContains == null)
        {
            return true;
        }

        /*
        * Check that the cert contains the specified value.
        */

        if(name.contains(this.certContains))
        {
            if(logger.isDebugEnabled())
            {
                logger.debug("Cert: "+ name + "  contains:  "+ this.certContains);
            }

            return true;
        }
        else
        {
            logger.error("Cert: " + name + "  does not contain:  " + this.certContains);
            return false;
        }
    }

    public void destroy()
    {
    }
}