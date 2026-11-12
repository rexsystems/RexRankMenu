const root = "rexrankmenu";

export const rexrankmenu = {
  label: "RexRankMenu",
  items: [
    { label: "Overview", slug: root },
    {
      label: "Getting started",
      items: [
        { label: "Installation", slug: `${root}/getting-started/installation` },
        { label: "Requirements", slug: `${root}/getting-started/requirements` },
      ],
    },
    {
      label: "Configuration",
      items: [
        { label: "Settings", slug: `${root}/configuration/overview` },
        { label: "Ranks", slug: `${root}/configuration/ranks` },
        { label: "Messages", slug: `${root}/configuration/messages` },
      ],
    },
    {
      label: "Guides",
      items: [
        { label: "LuckPerms track", slug: `${root}/guides/luckperms-track` },
      ],
    },
    {
      label: "Reference",
      items: [
        { label: "Commands", slug: `${root}/reference/commands` },
        { label: "Permissions", slug: `${root}/reference/permissions` },
      ],
    },
    { label: "FAQ", slug: `${root}/faq` },
  ],
};
