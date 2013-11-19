import javax.jcr.Node;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.commons.lang.StringUtils;
import com.day.cq.commons.jcr.JcrUtil;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

def ACTION_TYPE = "com/cognifide/actions/myExampleActionType";

def path_="/content/usergenerated/actions/dummyPath_"

// stres-test
def NumNodesToCreate=10;
maxStringLength=1; //...value * "lorem ipsum dolor sit amet consectetur adipiscing elit."


//main loop
for (int i = 1; i < NumNodesToCreate; i++) {
	def pat=path_+i;
	
	createActionNode(pat,ACTION_TYPE,i)
	session.save();
}





def createActionNode(relPath,type,num){
	path = createPath(relPath);
	System.out.println(path);
	Node page = JcrUtil.createPath(path, true, "sling:Folder", "cq:Page", session, false);
	Node content = page.addNode("jcr:content", "cq:PageContent");
	content.setProperty("sling:resourceType", type);
	content.setProperty("cq:distribute", false);
	session.save();
	content.setProperty("custom", num);
	content.setProperty("cq:lastModified", Calendar.getInstance());
	content.setProperty("cq:lastModifiedBy", session.getUserID());
	content.setProperty("cq:distribute", true);
}


def createPath(relPath) {
	String path;
	if (StringUtils.startsWith(relPath, "/")) {
		path = relPath;
	} else {
		final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd/");
		path = actionRoot + dateFormat.format(new Date()) + relPath;
	}

	if (path.endsWith("/*")) {
		String generated = StringUtils.EMPTY + new Date().getTime() + "-" + random.nextInt(100);
		path = StringUtils.removeEnd(path, "*") + generated;
	}
	return path;
}

def bigDummyString(){
	String s="";
	def end=Math.random()*maxStringLength;

	for(i=0;i<end;i++){
		s=s+"lorem ipsum dolor sit amet consectetur adipiscing elit.";
	}
	return s;
}
