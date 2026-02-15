import { ComponentExample } from "@/components/component-example";
import { Player } from "@/components/player";

export default function Page() {
  return (
    <>
      <Player videoUrl="http://localhost:8080/video/69878fe3-69fb-491d-9e8f-606b46c050d1-robo/master.m3u8" />
    </>
  );
// return <ComponentExample />;
}