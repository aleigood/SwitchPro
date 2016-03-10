package alei.switchpro.load;

public class XmlEntity
{
    private String btnIds;
    private String layoutName;
    private int iconColor;
    private int iconTrans;
    private int indColor;
    private int backColor;
    private int dividerColor;

    public String getBtnIds()
    {
        return btnIds;
    }

    public void setBtnIds(String btnIds)
    {
        this.btnIds = btnIds;
    }

    public String getLayoutName()
    {
        return layoutName;
    }

    public void setLayoutName(String layoutName)
    {
        this.layoutName = layoutName;
    }

    public int getIconColor()
    {
        return iconColor;
    }

    public void setIconColor(int iconColor)
    {
        this.iconColor = iconColor;
    }

    public int getIndColor()
    {
        return indColor;
    }

    public void setIndColor(int indColor)
    {
        this.indColor = indColor;
    }

    public int getIconTrans()
    {
        return iconTrans;
    }

    public void setIconTrans(int iconTrans)
    {
        this.iconTrans = iconTrans;
    }

    public int getBackColor()
    {
        return backColor;
    }

    public void setBackColor(int backColor)
    {
        this.backColor = backColor;
    }

    public void setDividerColor(int dividerColor)
    {
        this.dividerColor = dividerColor;
    }

    public int getDividerColor()
    {
        return dividerColor;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((btnIds == null) ? 0 : btnIds.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        XmlEntity other = (XmlEntity) obj;
        if (btnIds == null)
        {
            if (other.btnIds != null)
                return false;
        }
        else if (!btnIds.equals(other.btnIds))
            return false;
        return true;
    }
}
