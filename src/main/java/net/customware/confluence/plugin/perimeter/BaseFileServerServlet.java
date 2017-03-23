package net.customware.confluence.plugin.perimeter;

import com.atlassian.plugin.servlet.DownloadStrategy;
import com.atlassian.plugin.webresource.servlet.PluginResourceDownload;
import com.atlassian.plugin.servlet.ResourceDownloadUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public abstract class BaseFileServerServlet extends HttpServlet
{

    public static final String PATH_SEPARATOR = "/";
    public static final String RESOURCE_URL_PREFIX = "resources";

    private static List<Class<? extends DownloadStrategy>> downloadStrategies = Collections.synchronizedList(new ArrayList<Class<? extends DownloadStrategy>>());
    private static final Logger log = LoggerFactory.getLogger(BaseFileServerServlet.class);

    static
    {
    	downloadStrategies.add(PluginResourceDownload.class);
    }

    public static String SERVLET_PATH = "download";

    //~ Methods --------------------------------------------------------------------------------------------------------

    @Override
    public void init() throws ServletException
    {
        super.init();
    }

    @Override
    public void init(ServletConfig servletConfig) throws ServletException
    {
        super.init(servletConfig);
    }

    public String getMimeType(File fileToServe)
    {
        return getServletContext().getMimeType(fileToServe.getAbsolutePath());
    }

    /**
     * @deprecated Use {@link ResourceDownloadUtils#serveFileImpl(javax.servlet.http.HttpServletResponse, java.io.InputStream)} instead
     */
    public void serveFileImpl(HttpServletResponse httpServletResponse, InputStream in) throws IOException
    {
        ResourceDownloadUtils.serveFileImpl(httpServletResponse, in);
    }

    public abstract String getDecodedPathInfo(HttpServletRequest httpServletRequest);

    protected abstract DownloadStrategy instantiateDownloadStrategy(Class<? extends DownloadStrategy> downloadStrategyClass);

    protected abstract String urlDecode(String url);

    protected abstract String getContentType(String location);

    @Override
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws ServletException, IOException
    {

        try
        {
            DownloadStrategy downloadStrategy = getDownloadStrategy(httpServletRequest);

            if (downloadStrategy != null)
            {
                downloadStrategy.serveFile(httpServletRequest, httpServletResponse);
            }
            else
            {
                httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "The file you were looking for was not found");
            }
        }
        catch (Throwable t)
        {
            log.info("Error while serving file ", t);
            t.printStackTrace();
            throw new ServletException(t);
        }
    }

    protected void addDownloadStrategy(Class<? extends DownloadStrategy> strategyClass)
    {
        downloadStrategies.add(strategyClass);
    }

    private DownloadStrategy getDownloadStrategy(HttpServletRequest httpServletRequest)
    {
    	// probably wrong to do this
//        String url = httpServletRequest.getRequestURI().toLowerCase();
        String url = httpServletRequest.getRequestURI();
        for (Class<? extends DownloadStrategy> downloadStrategyClass : downloadStrategies) {
            DownloadStrategy downloadStrategy = instantiateDownloadStrategy(downloadStrategyClass);
            if (downloadStrategy.matches(url))
            {
                return downloadStrategy;
            }
        }

        return null;
    }

}